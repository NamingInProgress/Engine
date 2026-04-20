package com.vke.core.vulkan.vertexconsumer;

import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.api.annotation.MethodReference;
import com.vke.api.assets.AssetHandle;
import com.vke.api.draw.Drawable;
import com.vke.api.draw.Vertex;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.handles.array.CombinedImageSamplerArrayHandle;
import com.vke.core.Context;
import com.vke.core.mesh.Mesh;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.vulkan.descriptor.dynamicalloc.DynamicDescriptorAllocator;
import com.vke.core.vulkan.pipeline.VulkanRenderPipeline;
import com.vke.core.vulkan.sampler.Samplers;
import com.vke.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;

public class BatchedVKVertexConsumer<T extends Vertex> extends AbstractVertexConsumer<T> {

    public static final int DEFAULT_BATCH_VERTEX_CAPACITY = 10_000;
    private final AssetHandle<RenderPipeline> pipeline;
    private final VulkanRenderPipeline vkPipeline;
    private final CombinedImageSamplerArrayHandle texHandle;
    private final DynamicDescriptorAllocator alloc;

    private final RecyclerArrayList<Batch> batches;
    private final RecyclerArrayList<CombinedImageSamplerArrayHandle> handleCache;
    private final int maxTexSlots;
    private final Texture missing;

    public BatchedVKVertexConsumer(Context context, VulkanRenderer renderer, T template, AssetHandle<RenderPipeline> pipeline,
                                   String texturesArrayUniformName) {
        this(context, renderer, template, pipeline, texturesArrayUniformName, BASE_VERTEX_COUNT, BASE_INDEX_COUNT);
    }

    public BatchedVKVertexConsumer(Context context, VulkanRenderer renderer, T template, AssetHandle<RenderPipeline> pipeline,
                                   String texturesArrayUniformName, int estVertexCount, int estIndexCount) {
        super(context.getEngine(), renderer, template, estVertexCount, estIndexCount);
        this.pipeline = pipeline;

        try {
            this.vkPipeline = ((VulkanRenderPipeline) pipeline.acquire(context));
            this.texHandle = vkPipeline.resolveUniform(texturesArrayUniformName);
            this.maxTexSlots = this.texHandle.cisBinding.textures.length;
            this.missing = Utils.MISSING_TEXTURE.acquire(context);
            ObjectIntHashMap<DescriptorType> counts = new  ObjectIntHashMap<>();
            counts.put(DescriptorType.COMBINED_IMAGE_SAMPLER, maxTexSlots);
            this.alloc = new DynamicDescriptorAllocator(context, renderer.getDevice(), 10, counts, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.batches = new RecyclerArrayList<>(3);
        this.handleCache = new RecyclerArrayList<>(10);
        this.batches.add(new Batch(maxTexSlots));
    }

    @Override
    public void beginFrame() {
        super.beginFrame();
        for (Batch b : this.batches.iter()) {
            b.recycle();
        }
        this.batches.clear();
        this.batches.virtualAdd();
        this.handleCache.clear();
        this.handleCache.virtualAdd();
    }

    @Override
    public void begin() {
        Batch lastBatch = this.batches.lastUnchecked();
        lastBatch.frozenIndex = lastBatch.currentMaxIndex;
    }

    @MethodReference
    private Batch newBatch() {
        return new Batch(maxTexSlots);
    }

    @MethodReference
    private CombinedImageSamplerArrayHandle copyHandle() {
        return alloc.copy(texHandle);
    }

    private Batch ensureBatch(@Nullable Texture usesTexture, T... vertices) {
        Batch lastBatch = this.batches.lastUnchecked();
        if (usesTexture == null) {
            int amt = lastBatch.canFit(vertices);
            if (amt < vertices.length) {
                return this.batches.getOrCreateElement(true, this::newBatch);
            } else {
                return lastBatch;
            }
        } else {
            if (lastBatch.canFitTexture(usesTexture)) {
                return lastBatch;
            } else {
                return this.batches.getOrCreateElement(true, this::newBatch);
            }
        }
    }

    private int addTextureToBatch(Batch batch, Texture texture) {
        if (batch.usedTextures.containsKey(texture)) return batch.usedTextures.get(texture);
        int texIndex = batch.usedTextures.size();
        batch.usedTextures.put(texture, texIndex);
        return texIndex;
    }

    @Override
    public void vertices(T... vertices) {
        Batch batch = ensureBatch(null, vertices);
        for (T vertex : vertices) {
            Texture tex = vertex.usesTexture();
            if (tex != null) {
                int texIdx = addTextureToBatch(batch, tex);
                vertex.setTextureId(texIdx);
            }
        }
        batch.vertices.add(vertices);
        batch.currentMaxIndex += vertices.length;
    }

    @Override
    public void vertices(Texture usesTexture, T... vertices) {
        Batch batch = ensureBatch(usesTexture, vertices);
        batch.vertices.add(vertices);
        int idx = addTextureToBatch(batch, usesTexture);
        for (T vertex : vertices) {
            vertex.setTextureId(idx);
        }
        batch.currentMaxIndex += vertices.length;
    }

    @Override
    public void indices(int... indices) {
        Batch batch = this.batches.lastUnchecked();
        for (int i = 0; i < indices.length; i++) {
            indices[i] += batch.frozenIndex;
        }
        batch.indices.add(indices);
    }

    @Override
    public void mesh(Mesh<T> mesh) {
        begin();
        vertices(mesh.getVertices());
        indices(mesh.getIndices());
    }

    @Override
    public void draw(DrawContext ctx) {
        for (Batch b : this.batches.iter()) {
            b.draw(ctx);
        }
    }

    @Override
    public void free() {
        super.free();
        this.alloc.free();
    }

    final class Batch implements Drawable {
        private final RecyclerArrayList<T> vertices;
        private final InstantResetIntArrayList indices  = new InstantResetIntArrayList();
        // Map<Texture, (Sampler, idx)>
        private final HashMap<Texture, Integer> usedTextures = new HashMap<>();
        private final int maxTextureSlots;
        private int currentMaxIndex = 0;
        private int frozenIndex = 0;

        @SafeVarargs
        Batch(int maxTextureSlots, T... ignore) {
            //very educated guess
            this.vertices = new RecyclerArrayList<>(DEFAULT_BATCH_VERTEX_CAPACITY, ignore);
            this.maxTextureSlots = maxTextureSlots;
        }

        public int canFit(T... vertices) {
            for (int i = 0; i < vertices.length; i++) {
                T vertex = vertices[i];
                int amtTexUsed = usedTextures.size();
                int thisUsesTex = vertex.usesTexture() == null ? 0 : 1;
                if (thisUsesTex == 1 && usedTextures.containsKey(vertex.usesTexture())) {
                    thisUsesTex--;
                }
                if (amtTexUsed + thisUsesTex > maxTextureSlots) {
                    return i;
                }
            }
            return vertices.length;
        }

        public boolean canFitTexture(Texture texture) {
            return usedTextures.containsKey(texture) || usedTextures.size() < maxTextureSlots;
        }

        @Override
        public void draw(DrawContext ctx) {
            BatchedVKVertexConsumer<T> bvc = BatchedVKVertexConsumer.this;
            bvc.putVertices(vertices.toArray());
            bvc.putIndices(indices.toArray());
            ctx.getCommandBuffer().bindPipeline(bvc.pipeline);
            var handle = bvc.handleCache.getOrCreateElement(true, bvc::copyHandle);
            usedTextures.forEach((texture, textureUsage) -> handle.set(texture, Samplers.LINEAR, textureUsage));

            // Fill it so vulkan shuts its bitch ass up
            for (int i = usedTextures.size(); i < maxTextureSlots; i++) {
                handle.set(missing, Samplers.LINEAR, i);
            }

            alloc.update(handle);
            alloc.bindDescriptors(ctx, pipeline, handle);
            bvc.submitDraw(ctx);
        }

        public void recycle() {
            this.vertices.clear();
            this.indices.clear();
            this.usedTextures.clear();
            this.frozenIndex = 0;
            this.currentMaxIndex = 0;
        }
    }
}

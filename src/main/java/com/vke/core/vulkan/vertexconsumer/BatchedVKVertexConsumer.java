package com.vke.core.vulkan.vertexconsumer;

import com.vke.api.assets.AssetHandle;
import com.vke.api.draw.Drawable;
import com.vke.api.draw.Vertex;
import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.descriptors.handles.array.CombinedImageSamplerArrayHandle;
import com.vke.core.Context;
import com.vke.core.mesh.Mesh;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.vulkan.pipeline.VulkanRenderPipeline;
import com.vke.core.vulkan.sampler.Samplers;
import com.vke.utils.Utils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class BatchedVKVertexConsumer<T extends Vertex> extends AbstractVertexConsumer<T> {

    private final AssetHandle<RenderPipeline> pipeline;
    private final VulkanRenderPipeline vkPipeline;
    private final CombinedImageSamplerArrayHandle texHandle;

    private final InstantResetArrayList<Batch> batches;
    private final int maxTexSlots;
    private final Texture missing;

    private int frozenIndex = 0;

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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.batches = new InstantResetArrayList<>(3, new Batch(0));
        this.batches.add(new Batch(maxTexSlots));
    }

    @Override
    public void beginFrame() {
        super.beginFrame();
        for (Batch b : this.batches.iter()) {
            b.recycle();
        }
        this.batches.clear();
    }

    @Override
    public void begin() {

    }

    private Batch ensureBatch(@Nullable Texture usesTexture, T... vertices) {
        Batch lastBatch = this.batches.lastUnchecked();
        if (usesTexture == null) {
            int amt = lastBatch.canFit(vertices);
            if (amt < vertices.length) {
                return getOrCreateBatch();
            } else {
                return lastBatch;
            }
        } else {
            if (lastBatch.canFitTexture(usesTexture)) {
                return lastBatch;
            } else {
                return getOrCreateBatch();
            }
        }
    }

    private Batch getOrCreateBatch() {
        if (!this.batches.wasVeryLastElement()) {
            Batch newBatch = this.batches.get(this.batches.len());
            this.batches.virtualAdd();
            return newBatch;
        } else {
            Batch newBatch = new Batch(maxTexSlots);
            this.batches.add(newBatch);
            return newBatch;
        }
    }

    private void addTextureToBatch(Batch batch, Texture texture) {
        int texIndex = batch.usedTextures.size();
        batch.usedTextures.put(texture, texIndex);
    }

    @Override
    public void vertices(T... vertices) {
        Batch batch = ensureBatch(null, vertices);
        for (T vertex : vertices) {
            Texture tex = vertex.usesTexture();
            if (tex != null) {
                addTextureToBatch(batch, tex);
            }
        }
        batch.vertices.add(vertices);
    }

    @Override
    public void vertices(Texture usesTexture, T... vertices) {
        Batch batch = ensureBatch(usesTexture, vertices);
        batch.vertices.add(vertices);
        addTextureToBatch(batch, usesTexture);
    }

    @Override
    public void indices(int... indices) {
        Batch batch = this.batches.lastUnchecked();
        batch.indices.add(indices);
    }

    @Override
    public void mesh(Mesh<T> mesh) {
        vertices(mesh.getVertices());
        indices(mesh.getIndices());
    }

    @Override
    public void draw(DrawContext ctx) {
        for (Batch b : this.batches.iter()) {
            b.draw(ctx);
        }
    }

    final class Batch implements Drawable {
        private final InstantResetArrayList<T> vertices;
        private final InstantResetIntArrayList indices  = new InstantResetIntArrayList();
        // Map<Texture, (Sampler, idx)>
        private final HashMap<Texture, Integer> usedTextures = new HashMap<>();
        private final int maxTextureSlots;

        @SafeVarargs
        Batch(int maxTextureSlots, T... ignore) {
            //very educated guess
            int vertexCap = new Random().nextInt(2_000, 100_000);
            this.vertices = new InstantResetArrayList<>(vertexCap, ignore);
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
            bvc.begin();
            bvc.putVertices(vertices.toArray());
            bvc.putIndices(indices.toArray());
            ctx.getCommandBuffer().bindPipeline(bvc.pipeline);
            usedTextures.forEach((texture, textureUsage) -> bvc.texHandle.set(texture, Samplers.LINEAR, textureUsage));
            for (int i = usedTextures.size(); i < maxTextureSlots; i++) {
                bvc.texHandle.set(missing, Samplers.LINEAR, i);
            }
            bvc.vkPipeline.updateUniforms(bvc.texHandle);
            ctx.getCommandBuffer().bindDescriptorSets(bvc.pipeline);
            bvc.submitDraw(ctx);
        }

        public void recycle() {
            this.vertices.clear();
            this.indices.clear();
            this.usedTextures.clear();
        }
    }

    private record TextureUsage(Sampler sampler, int index) { }
}

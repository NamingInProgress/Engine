package com.vke.core.vulkan.draw;

import com.carrotsearch.hppc.IntArrayList;
import com.vke.api.assets.AssetHandle;
import com.vke.api.draw.*;
import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.descriptors.handles.array.CombinedImageSamplerArrayHandle;
import com.vke.core.Context;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.vulkan.pipeline.VulkanRenderPipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ShapeRenderer<T extends Vertex> implements Drawable {

    private final Context context;
    private final VertexConsumer<T> consumer;
    private final VertexFactory<T> factory;
    private final AssetHandle<RenderPipeline> pipeline;
    private final CombinedImageSamplerArrayHandle texHandle;

    private final ArrayList<Batch> batches = new ArrayList<>();

    // x y z r g b a u v i
    public ShapeRenderer(Context context, VertexConsumer<T> consumer, VertexFactory<T> factory,
                         AssetHandle<RenderPipeline> pipeline, String texturesArrayUniformName) {
        this.context = context;
        this.consumer = consumer;
        this.factory = factory;
        this.pipeline = pipeline;
        // move pipeline as well
        try {
            this.texHandle = ((VulkanRenderPipeline) pipeline.acquire(context)).resolveUniform(texturesArrayUniformName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void rect(float x, float y, float width, float height, int z, int r, int g, int b, int a, QuadTexture tex, Sampler sampl) {
//        Batch current = currentBatch();
//        if (current.textureCount() == 16) current = newBatch();
//
//        int idx = tex.texture() == null ? -1 : current.getOrCreateIndex(tex.texture(), sampl);
        /// THIS IS NOW IN BATCHED
        float[] uv = tex.uvFor();
        Texture texture = tex.texture();
        consumer.begin();
        consumer.vertices(factory.apply(x, y, z, r, g, b, a, uv[0], uv[1], texture));
        consumer.vertices(factory.apply(x + width, y, z, r, g, b, a, uv[0] + uv[2], uv[1], texture));
        consumer.vertices(factory.apply(x, y + height, z, r, g, b, a, uv[0], uv[1] + uv[3], texture));
        consumer.vertices(factory.apply(x + width, y + height, z, r, g, b, a, uv[0] + uv[2], uv[1] + uv[3], texture));
        consumer.indices(0, 1, 2, 3, 2, 0);
    }

    private void bindTextures(Batch batch) {

    }

    @Override
    public void draw(DrawContext ctx) {
        consumer.draw(ctx);
    }

    @Override
    public void bindIBO(DrawContext ctx) {
        consumer.bindIBO(ctx);
    }

    @Override
    public void bindVBO(DrawContext ctx) {
        consumer.bindVBO(ctx);
    }

    private Batch newBatch() {
        Batch batch = new Batch();
        batches.add(batch);
        return batch;
    }

    private Batch currentBatch() {
        int idx = batches.size() - 1;
        if (idx < 0) {
            Batch b = new Batch();
            this.batches.add(b);
            return b;
        }
        return batches.get(idx);
    }

    final static class Batch {
        private final HashMap<Key, Integer> map = new HashMap<>();
        private final ArrayList<Key> entries = new ArrayList<>();
        private final ArrayList<Vertex> vertices = new ArrayList<>();
        private final IntArrayList indices = new IntArrayList();

        private int textureCount() { return entries.size(); }

        private void indices(int... indices) {
            this.indices.add(indices);
        }

        private int getOrCreateIndex(Texture t, Sampler s) {
            Key key = new Key(t, s);
            Integer idx = map.get(key);
            if (idx != null) return idx;

            int newIndex = entries.size();
            map.put(key, newIndex);
            entries.add(key);
            return newIndex;
        }
    }

    final static class Key {
        Texture texture;
        Sampler sampler;

        public Key(Texture texture, Sampler sampler) {
            this.texture = texture;
            this.sampler = sampler;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(texture, key.texture) && Objects.equals(sampler, key.sampler);
        }

        @Override
        public int hashCode() {
            return Objects.hash(texture, sampler);
        }
    }

}

package com.vke.core.rendering.vertexconsumer;

import com.vke.api.draw.Vertex;
import com.vke.api.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.draw.VertexConsumerProvider;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.utils.io.Disposable;

import java.util.ArrayList;

public class VulkanVertexConsumerProvider implements VertexConsumerProvider {

    private final VKEngine engine;
    private final VulkanRenderer renderer;

    private final ArrayList<VertexConsumer<?>> CACHE = new ArrayList<>();

    public VulkanVertexConsumerProvider(Context ctx, VulkanRenderer renderer) {
        this.engine = ctx.getEngine();
        this.renderer = renderer;
    }

    @Override
    public <T extends Vertex> VertexConsumer<T> get(T template) {
        VertexConsumer<T> vc = new FastVertexConsumer<>(engine, renderer, template);
        CACHE.add(vc);
        return vc;
    }

    @Override
    public <T extends Vertex> VertexConsumer<T> get(T template, int estVertexCount, int estIndexCount) {
        VertexConsumer<T> vc = new FastVertexConsumer<>(engine, renderer, template, estVertexCount, estIndexCount);
        CACHE.add(vc);
        return vc;
    }

    @Override
    public void beginFrame() {
        CACHE.forEach(VertexConsumer::beginFrame);
    }

    @Override
    public void free() {
        CACHE.forEach(Disposable::free);
    }
}

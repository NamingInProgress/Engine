package com.vke.core.rendering.vertexconsumer;

import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.draw.VertexConsumerProvider;
import com.vke.core.framable.service.FramableManager;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import com.vke.core.services2.Services;
import com.vke.utils.io.Disposable;

import java.util.ArrayList;

public class VulkanVertexConsumerProvider implements VertexConsumerProvider {

    private final VulkanRenderSystem sys;

    private final ArrayList<VertexConsumer<?>> CACHE = new ArrayList<>();

    private final FramableManager fm;

    public VulkanVertexConsumerProvider(VulkanRenderSystem sys) {
        this.sys = sys;

        this.fm = sys.service(Services.FRAMABLE_MANAGER);
        this.fm.registerFramable(this);
    }

    @Override
    public <T extends Vertex> VertexConsumer<T> get(T template) {
        VertexConsumer<T> vc = new FastVertexConsumer<>(sys, template);
        CACHE.add(vc);
        return vc;
    }

    @Override
    public <T extends Vertex> VertexConsumer<T> get(T template, int estVertexCount, int estIndexCount) {
        VertexConsumer<T> vc = new FastVertexConsumer<>(sys, template, estVertexCount, estIndexCount);
        CACHE.add(vc);
        return vc;
    }

    @Override
    public void preFrame() {
        CACHE.forEach(VertexConsumer::beginFrame);
    }

    @Override
    public void free() {
        this.fm.removeFramable(this);
        CACHE.forEach(Disposable::free);
    }
}

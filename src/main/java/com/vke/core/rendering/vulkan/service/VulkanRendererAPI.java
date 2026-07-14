package com.vke.core.rendering.vulkan.service;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.renderer.RenderDevice;
import com.vke.api.rendering.abstraction.renderer.RenderResourceManager;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.rendering.abstraction.draw.VertexConsumerProvider;
import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.services2.Services;

public class VulkanRendererAPI extends ServiceAPI implements Renderer {
    public VulkanRendererAPI(ServiceImpl baseImpl) {
        super(Services.RENDERER, baseImpl);
    }

    private Renderer getImpl() {
        return (Renderer) getImplementation();
    }

    @Override
    public RenderDevice getDevice() {
        return getImpl().getDevice();
    }

    @Override
    public FrameCounter getFrameCounter() {
        return getImpl().getFrameCounter();
    }

    @Override
    public RenderSystem renderSystem() {
        return getImpl().renderSystem();
    }

    @Override
    public RenderResourceManager resourceManager() {
        return getImpl().resourceManager();
    }

    @Override
    public VertexConsumerProvider getVertexConsumerProvider() {
        return getImpl().getVertexConsumerProvider();
    }

    @Override
    public void beforeTerminate() {
        getImpl().beforeTerminate();
    }

}

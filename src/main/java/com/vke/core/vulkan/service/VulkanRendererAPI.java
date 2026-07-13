package com.vke.core.vulkan.service;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.RenderDevice;
import com.vke.api.rendering.abstraction.RenderResourceManager;
import com.vke.api.rendering.abstraction.RenderSystem;
import com.vke.api.rendering.abstraction.Renderer;
import com.vke.api.rendering.abstraction.data.FrameDataManager;
import com.vke.api.rendering.abstraction.data.TextureManager;
import com.vke.api.rendering.abstraction.draw.VertexConsumerProvider;
import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.services2.Services;
import com.vke.core.vulkan.texture.texture2.VulkanTexture;

public class VulkanRendererAPI extends ServiceAPI implements Renderer {
    public VulkanRendererAPI(ServiceImpl baseImpl) {
        super(Services.VULKAN_RENDERER, baseImpl);
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

}

package com.vke.core.vulkan.service;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.RenderDevice;
import com.vke.api.rendering.abstraction.Renderer;
import com.vke.api.rendering.abstraction.data.ITextureManager;
import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.services2.Services;

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
    public ITextureManager textureManager() {
        return getImpl().textureManager();
    }
}

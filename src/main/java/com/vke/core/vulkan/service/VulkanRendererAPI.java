package com.vke.core.vulkan.service;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.RenderDevice;
import com.vke.api.rendering.abstraction.LowRenderer;
import com.vke.api.rendering.abstraction.data.IFrameDataManager;
import com.vke.api.rendering.abstraction.data.ITextureManager;
import com.vke.api.rendering.abstraction.draw.VertexConsumerProvider;
import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.services2.Services;
import com.vke.core.vulkan.texture.texture2.VulkanTexture;

public class VulkanRendererAPI extends ServiceAPI implements LowRenderer {
    public VulkanRendererAPI(ServiceImpl baseImpl) {
        super(Services.VULKAN_RENDERER, baseImpl);
    }

    private LowRenderer getImpl() {
        return (LowRenderer) getImplementation();
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

    @Override
    public IFrameDataManager frameDataManager() {
        return getImpl().frameDataManager();
    }

    @Override
    public VertexConsumerProvider getVertexConsumerProvider() {
        return getImpl().getVertexConsumerProvider();
    }

    @Override
    public VulkanTexture renderTarget() {
        return (VulkanTexture) getImpl().renderTarget();
    }

    @Override
    public VulkanTexture depthTarget() {
        return (VulkanTexture) getImpl().depthTarget();
    }
}

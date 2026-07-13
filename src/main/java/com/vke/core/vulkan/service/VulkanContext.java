package com.vke.core.vulkan.service;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.IRendererContext;
import com.vke.core.Context;
import com.vke.core.ContextWrapper;
import com.vke.core.rendering.draw.VulkanFrameDataManager;
import com.vke.core.rendering.texture.VulkanTextureManager;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.swapchain.VulkanSwapchain;

public class VulkanContext extends ContextWrapper implements IRendererContext {
    private final long windowHandle;
    private final VulkanRenderer renderer;
    private final VulkanSwapchain swapchain;
    private final VulkanRenderDevice device;

    public VulkanContext(Context baseContext, VulkanRenderer renderer) {
        super(baseContext);
        this.renderer = renderer;
        this.swapchain = renderer.swapchain;
        this.device = renderer.device;
        this.windowHandle = getEngine().getWindow().getHandle();
    }

    @Override
    public VulkanRenderer renderer() {
        return this.renderer;
    }

    @Override
    public VulkanRenderDevice device() {
        return this.device;
    }

    @Override
    public VulkanSwapchain swapchain() {
        return this.swapchain;
    }

    @Override
    public VulkanTextureManager textureManager() {
        return this.renderer.getEngineSetsManager().textureManager;
    }

    @Override
    public VulkanFrameDataManager frameDataManager() {
        return this.renderer.getEngineSetsManager().frameDataManager;
    }

    @Override
    public FrameCounter getFrameCounter() {
        return this.renderer.frameCounter;
    }

    @Override
    public long windowHandle() {
        return this.windowHandle;
    }
}

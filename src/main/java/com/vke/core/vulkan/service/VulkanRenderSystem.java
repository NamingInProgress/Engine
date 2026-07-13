package com.vke.core.vulkan.service;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.RenderSystem;
import com.vke.core.Context;
import com.vke.core.ContextWrapper;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.draw.VulkanFrameDataManager;
import com.vke.core.rendering.texture.VulkanTextureManager;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.swapchain.VulkanSwapchain;

public class VulkanRenderSystem extends RenderSystem {
    private final long windowHandle;
    private final VulkanRenderer renderer;

    public VulkanRenderSystem(Context baseContext, VulkanRenderer renderer) {
        super(baseContext);
        this.renderer = renderer;
        this.windowHandle = getEngine().getWindow().getHandle();
    }

    @Override
    public VulkanRenderer renderer() {
        return this.renderer;
    }

    @Override
    public VulkanRenderDevice device() {
        return this.renderer.device;
    }

    @Override
    public VulkanSwapchain swapchain() {
        return this.renderer.swapchain;
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
    public VulkanCmdBuffers getCurrentCommandBuffer() {
        return this.renderer.frameData.cmd();
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

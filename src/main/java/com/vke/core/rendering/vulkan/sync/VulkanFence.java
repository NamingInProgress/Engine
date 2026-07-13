package com.vke.core.rendering.vulkan.sync;

import com.vke.api.rendering.abstraction.sync.Fence;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkFenceCreateInfo;

import java.nio.LongBuffer;

public class VulkanFence implements Fence {

    private final long handle;
    private final LogicalDevice device;

    public VulkanFence(VulkanRenderSystem ctx) {
        this.device = ctx.device().getLogicalDevice();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pFence = stack.mallocLong(1);
            if (VK14.vkCreateFence(device.getDevice(), getDefaultCreateInfo(stack), null, pFence) != VK14.VK_SUCCESS) {
                throw new IllegalStateException("Failed to create fence!");
            }

            this.handle = pFence.get(0);
        }
    }

    public VkFenceCreateInfo getDefaultCreateInfo(MemoryStack stack) {
        VkFenceCreateInfo info = VkFenceCreateInfo.calloc(stack);
        info.sType$Default();
        info.flags(VK14.VK_FENCE_CREATE_SIGNALED_BIT);
        return info;
    }

    @Override
    public boolean isSignaled() {
        return VK14.vkGetFenceStatus(device.getDevice(), handle) == VK14.VK_SUCCESS;
    }

    @Override
    public void waitForFence() {
        waitForFence(Integer.MAX_VALUE);
    }

    @Override
    public void reset() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VK14.vkResetFences(device.getDevice(), stack.longs(handle));
        }
    }

    @Override
    public boolean waitForFence(long timeout) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            return VK14.vkWaitForFences(device.getDevice(), stack.longs(handle), true, timeout) == VK14.VK_SUCCESS;
        }
    }

    public long getHandle() {
        return handle;
    }

    @Override
    public void free() {
        VK14.vkDestroyFence(device.getDevice(), handle, null);
    }

}

package com.vke.core.rendering.vulkan.sync;

import com.vke.api.rendering.abstraction.sync.Semaphore;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreSubmitInfo;

import java.nio.LongBuffer;

public class VulkanSemaphore implements Semaphore {

    private final long handle;
    private final LogicalDevice device;

    private VulkanSemaphore(VulkanRenderSystem ctx, long handle) {
        this.handle = handle;
        this.device = ctx.device().getLogicalDevice();
    }

    public long getHandle() { return this.handle; }


    /** BUILDER **/
    public static VkSemaphoreCreateInfo getDefaultCreateInfo(MemoryStack stack) {
        VkSemaphoreCreateInfo info = VkSemaphoreCreateInfo.calloc(stack);
        info.flags(0);
        info.sType$Default();
        return info;
    }

    public static VkSemaphoreSubmitInfo getDefaultSubmitInfo(MemoryStack stack, VulkanSemaphore semaphore, int VkPipelineStageFlags2) {
        VkSemaphoreSubmitInfo submitInfo = VkSemaphoreSubmitInfo.calloc(stack);
        submitInfo.sType$Default();
        submitInfo.stageMask(VkPipelineStageFlags2);
        submitInfo.semaphore(semaphore.getHandle());
        return submitInfo;
    }

    public static VulkanSemaphore createSemaphore(VulkanRenderSystem ctx, VkSemaphoreCreateInfo createInfo) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pSemaphore = stack.mallocLong(1);
            if (VK14.vkCreateSemaphore(ctx.device().vkLogicalDevice(), createInfo, null, pSemaphore) != VK14.VK_SUCCESS) {
                ctx.throwException(new IllegalStateException("Failed to create semaphore!"), "SEMAPHORE_createSemaphore");
            }

            return new VulkanSemaphore(ctx, pSemaphore.get(0));
        }
    }

    public static VulkanSemaphore createSemaphore(VulkanRenderSystem ctx) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var i = VkSemaphoreCreateInfo.calloc(stack);
            i.flags(0);
            i.sType$Default();

            LongBuffer pSemaphore = stack.mallocLong(1);
            if (VK14.vkCreateSemaphore(ctx.device().vkLogicalDevice(), i, null, pSemaphore) != VK14.VK_SUCCESS) {
                ctx.throwException(new IllegalStateException("Failed to create semaphore!"), "SEMAPHORE_createSemaphore");
            }

            return new VulkanSemaphore(ctx, pSemaphore.get(0));
        }
    }

    @Override
    public void free() {
        VK14.vkDestroySemaphore(device.getDevice(), handle, null);
    }
}

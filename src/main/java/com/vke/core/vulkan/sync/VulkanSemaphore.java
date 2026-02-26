package com.vke.core.vulkan.sync;

import com.vke.api.rendering.abstraction.sync.Semaphore;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.device.LogicalDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreSubmitInfo;

import java.nio.LongBuffer;

public class VulkanSemaphore implements Semaphore {

    private final long handle;
    private final LogicalDevice device;

    private VulkanSemaphore(LogicalDevice device, long handle) {
        this.handle = handle;
        this.device = device;
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

    public static VulkanSemaphore createSemaphore(VKEngine engine, LogicalDevice device, VkSemaphoreCreateInfo createInfo) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pSemaphore = stack.mallocLong(1);
            if (VK14.vkCreateSemaphore(device.getDevice(), createInfo, null, pSemaphore) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create semaphore!"), "SEMAPHORE_createSemaphore");
            }

            return new VulkanSemaphore(device, pSemaphore.get(0));
        }
    }

    public static VulkanSemaphore createSemaphore(VKEngine engine, LogicalDevice device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var i = VkSemaphoreCreateInfo.calloc(stack);
            i.flags(0);
            i.sType$Default();

            LongBuffer pSemaphore = stack.mallocLong(1);
            if (VK14.vkCreateSemaphore(device.getDevice(), i, null, pSemaphore) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create semaphore!"), "SEMAPHORE_createSemaphore");
            }

            return new VulkanSemaphore(device, pSemaphore.get(0));
        }
    }

    @Override
    public void free() {
        VK14.vkDestroySemaphore(device.getDevice(), handle, null);
    }
}

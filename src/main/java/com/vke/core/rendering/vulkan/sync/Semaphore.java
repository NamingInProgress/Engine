package com.vke.core.rendering.vulkan.sync;

import com.vke.core.VKEngine;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreSubmitInfo;

import java.nio.LongBuffer;

public class Semaphore implements Disposable {

    private final long handle;
    private final LogicalDevice device;

    private Semaphore(LogicalDevice device, long handle) {
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

    public static VkSemaphoreSubmitInfo getDefaultSubmitInfo(MemoryStack stack, Semaphore semaphore, int VkPipelineStageFlags2) {
        VkSemaphoreSubmitInfo submitInfo = VkSemaphoreSubmitInfo.calloc(stack);
        submitInfo.sType$Default();
        submitInfo.stageMask(VkPipelineStageFlags2);
        submitInfo.semaphore(semaphore.getHandle());
        return submitInfo;
    }

    public static Semaphore createSemaphore(VKEngine engine, LogicalDevice device, VkSemaphoreCreateInfo createInfo) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pSemaphore = stack.mallocLong(1);
            if (VK14.vkCreateSemaphore(device.getDevice(), createInfo, null, pSemaphore) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create semaphore!"), "SEMAPHORE_createSemaphore");
            }

            return new Semaphore(device, pSemaphore.get(0));
        }
    }

    public static Semaphore createSemaphore(VKEngine engine, LogicalDevice device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var i = VkSemaphoreCreateInfo.calloc(stack);
            i.flags(0);
            i.sType$Default();

            LongBuffer pSemaphore = stack.mallocLong(1);
            if (VK14.vkCreateSemaphore(device.getDevice(), i, null, pSemaphore) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create semaphore!"), "SEMAPHORE_createSemaphore");
            }

            return new Semaphore(device, pSemaphore.get(0));
        }
    }

    @Override
    public void free() {
        VK14.vkDestroySemaphore(device.getDevice(), handle, null);
    }
}

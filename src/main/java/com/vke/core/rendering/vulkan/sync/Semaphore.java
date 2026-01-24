package com.vke.core.rendering.vulkan.sync;

import com.vke.core.VKEngine;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

import java.nio.LongBuffer;

public class Semaphore {

    private final long handle;

    private Semaphore(long handle) {
        this.handle = handle;
    }

    public long getHandle() { return this.handle; }

    private static final AutoHeapAllocator alloc = new AutoHeapAllocator();
    private static VkSemaphoreCreateInfo info;

    public static VkSemaphoreCreateInfo getDefaultCreateInfo() {
        if (info == null) {
            info = alloc.allocStruct(VkSemaphoreCreateInfo.SIZEOF, VkSemaphoreCreateInfo::new);
            info.sType$Default();
        }
        return info;
    }

    public static Semaphore createSemaphore(VKEngine engine, LogicalDevice device, VkSemaphoreCreateInfo createInfo) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pSemaphore = stack.mallocLong(1);
            if (VK14.vkCreateSemaphore(device.getDevice(), createInfo, null, pSemaphore) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create semaphore!"), "SEMAPHORE_createSemaphore");
            }

            return new Semaphore(pSemaphore.get(0));
        }
    }

    public static Semaphore createSemaphore(VKEngine engine, LogicalDevice device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pSemaphore = stack.mallocLong(1);
            if (VK14.vkCreateSemaphore(device.getDevice(), getDefaultCreateInfo(), null, pSemaphore) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create semaphore!"), "SEMAPHORE_createSemaphore");
            }

            return new Semaphore(pSemaphore.get(0));
        }
    }

    public static void freeCreateInfo() {
        alloc.close();
    }

}

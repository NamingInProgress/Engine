package com.vke.core.rendering.vulkan.sync;

import com.vke.core.VKEngine;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkFenceCreateInfo;

import java.nio.LongBuffer;

public class Fence {

    private final long handle;

    private Fence(long handle) {
        this.handle = handle;
    }

    public long getHandle() { return this.handle; }

    private static final AutoHeapAllocator alloc = new AutoHeapAllocator();
    private static VkFenceCreateInfo info;

    public static VkFenceCreateInfo getDefaultCreateInfo() {
        if (info == null) {
            info = alloc.allocStruct(VkFenceCreateInfo.SIZEOF, VkFenceCreateInfo::new);
            info.sType$Default();
        }
        return info;
    }

    public static Fence createFence(VKEngine engine, LogicalDevice device, VkFenceCreateInfo createInfo) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pFence = stack.mallocLong(1);
            if (VK14.vkCreateFence(device.getDevice(), createInfo, null, pFence) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create fence!"), "FENCE_createFence");
            }

            return new Fence(pFence.get(0));
        }
    }

    public static Fence createFence(VKEngine engine, LogicalDevice device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pFence = stack.mallocLong(1);
            if (VK14.vkCreateFence(device.getDevice(), getDefaultCreateInfo(), null, pFence) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create fence!"), "FENCE_createFence");
            }

            return new Fence(pFence.get(0));
        }
    }

    public static void freeCreateInfo() {
        alloc.close();
    }

}

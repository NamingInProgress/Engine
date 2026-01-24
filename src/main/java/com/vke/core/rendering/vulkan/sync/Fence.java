package com.vke.core.rendering.vulkan.sync;

import com.vke.core.VKEngine;
import com.vke.core.memory.AutoHeapAllocator;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkFenceCreateInfo;

import java.nio.LongBuffer;
import java.util.Arrays;

public class Fence implements Disposable {

    private final long handle;
    private final LogicalDevice device;

    private Fence(LogicalDevice device, long handle) {
        this.handle = handle;
        this.device = device;
    }

    public void waitAndReset(MemoryStack stack, VKEngine engine, LogicalDevice device, int time) {
        Fence[] f = new Fence[] { this };
        waitForFences(stack, engine, device, f, true, time);
        resetFences(stack, engine, device, f);
    }

    public long getHandle() { return this.handle; }

    /** BUILDER **/
    private static final AutoHeapAllocator alloc = new AutoHeapAllocator();
    private static VkFenceCreateInfo info;

    public static VkFenceCreateInfo getDefaultCreateInfo() {
        if (info == null) {
            info = alloc.allocStruct(VkFenceCreateInfo.SIZEOF, VkFenceCreateInfo::new);
            info.sType$Default();
            info.flags(VK14.VK_FENCE_CREATE_SIGNALED_BIT);
        }
        return info;
    }

    public static Fence createFence(VKEngine engine, LogicalDevice device, VkFenceCreateInfo createInfo) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pFence = stack.mallocLong(1);
            if (VK14.vkCreateFence(device.getDevice(), createInfo, null, pFence) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create fence!"), "FENCE_createFence");
            }

            return new Fence(device, pFence.get(0));
        }
    }

    public static Fence createFence(VKEngine engine, LogicalDevice device) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pFence = stack.mallocLong(1);
            if (VK14.vkCreateFence(device.getDevice(), getDefaultCreateInfo(), null, pFence) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create fence!"), "FENCE_createFence");
            }

            return new Fence(device, pFence.get(0));
        }
    }

    public static void waitForFences(MemoryStack stack, VKEngine engine, LogicalDevice device, Fence[] fences, boolean waitForAll, long timeout) {
        LongBuffer fencesBuffer = stack.longs(Arrays.stream(fences).mapToLong(Fence::getHandle).toArray());
        if (VK14.vkWaitForFences(device.getDevice(), fencesBuffer, waitForAll, timeout) != VK14.VK_SUCCESS) {
            engine.getLogger().warn("Couldn't wait for fences");
        }
    }

    public static void resetFences(MemoryStack stack, VKEngine engine, LogicalDevice device, Fence[] fences) {
        LongBuffer fencesBuffer = stack.longs(Arrays.stream(fences).mapToLong(Fence::getHandle).toArray());
        if (VK14.vkResetFences(device.getDevice(), fencesBuffer) != VK14.VK_SUCCESS) {
            engine.getLogger().warn("Couldn't reset fences!");
        }
    }

    public static void freeCreateInfo() {
        alloc.close();
    }

    @Override
    public void free() {
        VK14.vkDestroyFence(device.getDevice(), handle, null);
    }
}

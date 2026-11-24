package com.vke.api.rendering.vulkan.memory;

import org.lwjgl.vulkan.VK14;

public record VulkanMemoryBarrier(long srcStage, long dstStage, long srcAccess, long dstAccess, int srcQueueFamily, int dstQueueFamily) {

    public VulkanMemoryBarrier(long srcStage, long dstStage, long srcAccess, long dstAccess) {
        this(srcStage, dstStage, srcAccess, dstAccess, VK14.VK_QUEUE_FAMILY_IGNORED, VK14.VK_QUEUE_FAMILY_IGNORED);
    }

}
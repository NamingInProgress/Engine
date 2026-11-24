package com.vke.api.rendering.vulkan.memory;

import org.lwjgl.vulkan.VK14;

public record VulkanBufferBarrier(VulkanMemoryBarrier barrier, long offset, long size) {

    public VulkanBufferBarrier(VulkanMemoryBarrier barrier) {
        this(barrier, 0, VK14.VK_WHOLE_SIZE);
    }

}

package com.vke.core.rendering.vulkan;

import org.lwjgl.vulkan.VkQueue;

import java.util.Objects;

public class VulkanQueue {
    private final VkQueue queue;
    private final int familyIndex;
    private final VkQueueType queueType;

    public VulkanQueue(VkQueue queue, int familyIndex, VkQueueType queueType) {
        Objects.requireNonNull(queueType, "Queue type must not be null!");
        this.queue = queue;
        this.familyIndex = familyIndex;
        this.queueType = queueType;
    }

    public VkQueueType getType() { return this.queueType; }
    public int index() { return this.familyIndex; }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof VulkanQueue )) return false;
        return familyIndex == ((VulkanQueue) other).familyIndex;
    }

    public static enum VkQueueType {
        GRAPHICS,
        COMPUTE,
        PRESENT
    }
}

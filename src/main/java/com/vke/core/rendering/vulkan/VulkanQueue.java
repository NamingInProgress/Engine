package com.vke.core.rendering.vulkan;

import org.lwjgl.vulkan.VkQueue;

import java.util.Objects;

public class VulkanQueue {
    private final VkQueue queue;
    private final int familyIndex;
    private final Type queueType;

    public VulkanQueue(VkQueue queue, int familyIndex, Type queueType) {
        Objects.requireNonNull(queueType, "Queue type must not be null!");
        this.queue = queue;
        this.familyIndex = familyIndex;
        this.queueType = queueType;
    }

    public Type getType() { return this.queueType; }
    public int index() { return this.familyIndex; }
    public VkQueue vk() { return queue; }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof VulkanQueue )) return false;
        return familyIndex == ((VulkanQueue) other).familyIndex;
    }

    public static enum Type {
        GRAPHICS,
        COMPUTE,
        PRESENT
    }
}

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

    public static enum VkQueueType {

        GRAPHICS,
        COMPUTE

    }

}

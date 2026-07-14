package com.vke.core.rendering.vulkan.device;

import com.vke.api.rendering.abstraction.renderer.enums.QueueType;
import org.lwjgl.vulkan.*;

import java.util.Objects;

public class VulkanQueue {
    private final VkQueue queue;
    private final int familyIndex;
    private final QueueType queueType;

    public VulkanQueue(VkQueue queue, int familyIndex, QueueType queueType) {
        Objects.requireNonNull(queueType, "Queue type must not be null!");
        this.queue = queue;
        this.familyIndex = familyIndex;
        this.queueType = queueType;
    }

    public QueueType getType() { return this.queueType; }
    public int index() { return this.familyIndex; }
    public VkQueue vk() { return queue; }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof VulkanQueue )) return false;
        return familyIndex == ((VulkanQueue) other).familyIndex;
    }

}

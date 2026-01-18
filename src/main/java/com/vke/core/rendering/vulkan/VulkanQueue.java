package com.vke.core.rendering.vulkan;

import org.lwjgl.vulkan.VkQueue;

public class VulkanQueue {
    private final VkQueue queue;
    private final int familyIndex;

    public VulkanQueue(VkQueue queue, int familyIndex) {
        this.queue = queue;
        this.familyIndex = familyIndex;
    }
}

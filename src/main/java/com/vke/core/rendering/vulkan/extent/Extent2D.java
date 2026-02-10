package com.vke.core.rendering.vulkan.extent;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;

public class Extent2D {
    public final int width;
    public final int height;

    public Extent2D(int widht, int height) {
        this.width = widht;
        this.height = height;
    }

    public static Extent2D ofVk(VkExtent2D vk) {
        return new Extent2D(vk.width(), vk.height());
    }

    public VkExtent2D createVk2D(MemoryStack stack) {
        return VkExtent2D.calloc(stack)
                .width(width)
                .height(height);
    }
}

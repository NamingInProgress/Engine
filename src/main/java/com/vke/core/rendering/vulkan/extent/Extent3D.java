package com.vke.core.rendering.vulkan.extent;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent3D;

public class Extent3D extends Extent2D {
    public final int depth;

    public Extent3D(int width, int height, int depth) {
        super(width, height);
        this.depth = depth;
    }

    public static Extent3D ofVk(VkExtent3D vk) {
        return new Extent3D(vk.width(), vk.height(), vk.depth());
    }

    public VkExtent3D createVk3D(MemoryStack stack) {
        return VkExtent3D.calloc(stack)
                .width(width)
                .height(height)
                .depth(depth);
    }
}

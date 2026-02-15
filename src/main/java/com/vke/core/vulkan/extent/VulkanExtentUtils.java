package com.vke.core.vulkan.extent;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkExtent3D;

public class VulkanExtentUtils {

    public static Extent2D ofVk(VkExtent2D vk) {
        return new Extent2D(vk.width(), vk.height());
    }

    public static VkExtent2D createVk2D(MemoryStack stack, Extent2D ext) {
        return VkExtent2D.calloc(stack)
                .width(ext.width)
                .height(ext.height);
    }

    public static Extent3D ofVk(VkExtent3D vk) {
        return new Extent3D(vk.width(), vk.height(), vk.depth());
    }

    public static VkExtent3D createVk3D(MemoryStack stack, Extent3D ext) {
        return VkExtent3D.calloc(stack)
                .width(ext.width)
                .height(ext.height)
                .depth(ext.depth);
    }

}

package com.vke.api.rendering.vulkan.memory;

import com.vke.api.rendering.abstraction.renderer.enums.texture.ImageAspect;
import com.vke.api.rendering.vulkan.ImageLayout;
import org.lwjgl.vulkan.VK14;

public record VulkanImageBarrier(VulkanMemoryBarrier barrier, ImageLayout oldLayout,
                                 ImageLayout newLayout, int baseMip, int mipCount, int baseLayer, int layerCount, ImageAspect aspect) {

    public VulkanImageBarrier(VulkanMemoryBarrier barrier, ImageLayout oldLayout, ImageLayout newLayout, ImageAspect aspect) {
        this(barrier, oldLayout, newLayout, 0,
                -1, 0, -1, aspect);
    }
}

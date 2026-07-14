package com.vke.api.rendering.vulkan.memory;

import com.vke.api.rendering.abstraction.renderer.enums.texture.ImageAspect;
import com.vke.api.rendering.vulkan.ImageLayout;
import org.lwjgl.vulkan.VK14;

public record VulkanImageBarrier(long srcStage, long dstStage, long srcAccess, long dstAccess, ImageLayout oldLayout,
                                 ImageLayout newLayout, int baseMip, int mipCount, int baseLayer, int layerCount,
                                 int srcQueueFamily, int dstQueueFamily, ImageAspect aspect) {

    public VulkanImageBarrier(long srcStage, long dstStage, long srcAccess, long dstAccess, ImageLayout oldLayout, ImageLayout newLayout,
                              int baseMip, int mipCount, int baseLayer, int layerCount, ImageAspect aspect) {
        this(srcStage, dstStage, srcAccess, dstAccess, oldLayout, newLayout, baseMip,
                mipCount, baseLayer, layerCount, VK14.VK_QUEUE_FAMILY_IGNORED, VK14.VK_QUEUE_FAMILY_IGNORED, aspect);
    }
}

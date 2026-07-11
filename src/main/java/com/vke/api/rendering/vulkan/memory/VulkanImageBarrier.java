package com.vke.api.rendering.vulkan.memory;

import com.vke.api.rendering.vulkan.ImageLayout;
import org.lwjgl.vulkan.VK14;

public record VulkanImageBarrier(int srcStage, int dstStage, int srcAccess, int dstAccess, ImageLayout oldLayout,
                                 ImageLayout newLayout, int baseMip, int mipCount, int baseLayer, int layerCount,
                                 int srcQueueFamily, int dstQueueFamily) {

    public VulkanImageBarrier(int srcStage, int dstStage, int srcAccess, int dstAccess, ImageLayout oldLayout, ImageLayout newLayout,
                              int baseMip, int mipCount, int baseLayer, int layerCount) {
        this(srcStage, dstStage, srcAccess, dstAccess, oldLayout, newLayout, baseMip,
                mipCount, baseLayer, layerCount, VK14.VK_QUEUE_FAMILY_IGNORED, VK14.VK_QUEUE_FAMILY_IGNORED);
    }
}

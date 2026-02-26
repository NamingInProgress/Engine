package com.vke.core.vulkan.texture;

import com.vke.api.rendering.vulkan.ImageLayout;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkDependencyInfo;
import org.lwjgl.vulkan.VkImageMemoryBarrier2;
import org.lwjgl.vulkan.VkImageSubresourceRange;

public class VKTextureUtil {

    public static void transitionLayout(VulkanCmdBuffers buffers, long texture, int aspectMask, int baseMip, int levelCount, int baseArray, int layerCount,
                                        ImageLayout oldLayout, ImageLayout newLayout,
                                        long srcAccessMask, long dstAccessMask,
                                        long srcStageMask, long dstStageMask) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            // TODO: FIX THIS FUCKASS SHIT HOLY FUCK I HATE THIS (RENDER DOC HAS ISSUES WITH EVERYTHIGN I DO)
            VkImageSubresourceRange range = VkImageSubresourceRange.calloc(stack)
                    .aspectMask(aspectMask)
                    .baseMipLevel(baseMip)
                    .levelCount(levelCount)
                    .baseArrayLayer(baseArray)
                    .layerCount(layerCount);


            VkImageMemoryBarrier2.Buffer barriers = VkImageMemoryBarrier2.calloc(1, stack);
            barriers.get(0)
                    .sType$Default()
                    .srcStageMask(srcStageMask)
                    .srcAccessMask(srcAccessMask)
                    .dstStageMask(dstStageMask)
                    .dstAccessMask(dstAccessMask)
                    .oldLayout(oldLayout.getVkHandle())
                    .newLayout(newLayout.getVkHandle())
                    .srcQueueFamilyIndex(VK14.VK_QUEUE_FAMILY_IGNORED)
                    .dstQueueFamilyIndex(VK14.VK_QUEUE_FAMILY_IGNORED)
                    .image(texture)
                    .subresourceRange(range);

            VkDependencyInfo dependencyInfo = VkDependencyInfo.calloc(stack)
                    .sType$Default()
                    .dependencyFlags(0)
                    .pImageMemoryBarriers(barriers);

            VK14.vkCmdPipelineBarrier2(buffers.getBuffer(), dependencyInfo);
        }
    }

    public static void transitionLayout(VulkanCmdBuffers buffers, long texture, int aspectMask, int baseMip, int levelCount, int baseArray, int layerCount,
                                        ImageLayout oldLayout, ImageLayout newLayout) {
        transitionLayout(buffers, texture, aspectMask,baseMip, levelCount, baseArray, layerCount, oldLayout, newLayout, VK14.VK_ACCESS_2_MEMORY_WRITE_BIT,
                VK14.VK_ACCESS_2_MEMORY_WRITE_BIT | VK14.VK_ACCESS_2_MEMORY_READ_BIT,
                VK14.VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT, VK14.VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT);
    }

    public static void transitionLayout(VulkanCmdBuffers buffers, VulkanImage image, ImageLayout newLayout) {
        transitionLayout(buffers, image.getHandle(), image.aspect.getVkHandle(), 0, image.mipLevels, 0, image.arrayLayers, image.layout, newLayout);
    }

}

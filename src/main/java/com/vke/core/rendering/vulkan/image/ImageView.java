package com.vke.core.rendering.vulkan.image;

import com.vke.api.vulkan.ImageLayout;
import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.commands.CommandBuffers;
import com.vke.core.vulkan.device.LogicalDevice;
import com.vke.core.rendering.vulkan.swapchain.SwapchainImage;
import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

public class ImageView implements Disposable {
    private long parent;
    private long handle;
    private ImageLayout layout;
    private LogicalDevice device;

    public ImageView(SwapchainImage parent, VKEngine engine, LogicalDevice device, VkImageViewCreateInfo info) {
        this.parent = parent.getHandle();
        this.device = device;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pImageView = stack.mallocLong(1);
            if (VK14.vkCreateImageView(device.getDevice(), info, null, pImageView) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create image view"), "Image View");
            }
            handle = pImageView.get(0);
            layout = ImageLayout.UNDEFINED;
        }

        MemoryUtil.nmemFree(info.address());
    }

    private ImageView(VulkanImage image, ImageLayout layout, LogicalDevice device, long handle) {
        this.parent = image.getHandle();
        this.layout = layout;
        this.device = device;
        this.handle = handle;
    }

    public static ImageView createFromImage(VulkanImage image, VKEngine engine, LogicalDevice device, VkImageViewCreateInfo info) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pImageView = stack.mallocLong(1);
            if (VK14.vkCreateImageView(device.getDevice(), info, null, pImageView) != VK14.VK_SUCCESS) {
                engine.throwException(new IllegalStateException("Failed to create image view"), "Image View");
            }
            long handle = pImageView.get(0);
            ImageLayout layout = ImageLayout.UNDEFINED;
            return new ImageView(image, layout, device, handle);
        }
    }

    public static VkImageViewCreateInfo copyCreateInfo(VkImageViewCreateInfo original) {
        int size = VkImageViewCreateInfo.SIZEOF;
        long newAddr = MemoryUtil.nmemCalloc(1, size);
        MemoryUtil.memCopy(original.address(), newAddr, size);
        return VkImageViewCreateInfo.create(newAddr);
    }

    public void transitionLayout(CommandBuffers buffers, ImageLayout newLayout, long srcAccessMask, long dstAccessMask, long srcStageMask, long dstStageMask) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkImageSubresourceRange range = VkImageSubresourceRange.calloc(stack)
                    .aspectMask(VK14.VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseMipLevel(0)
                    .levelCount(1)
                    .baseArrayLayer(0)
                    .layerCount(1);


            VkImageMemoryBarrier2.Buffer barriers = VkImageMemoryBarrier2.calloc(1, stack);
            barriers.get(0)
                    .sType$Default()
                    .srcStageMask(srcStageMask)
                    .srcAccessMask(srcAccessMask)
                    .dstStageMask(dstStageMask)
                    .dstAccessMask(dstAccessMask)
                    .oldLayout(layout.getVkHandle())
                    .newLayout(newLayout.getVkHandle())
                    .srcQueueFamilyIndex(VK14.VK_QUEUE_FAMILY_IGNORED)
                    .dstQueueFamilyIndex(VK14.VK_QUEUE_FAMILY_IGNORED)
                    .image(parent)
                    .subresourceRange(range);

            VkDependencyInfo dependencyInfo = VkDependencyInfo.calloc(stack)
                    .sType$Default()
                    .dependencyFlags(0)
                    .pImageMemoryBarriers(barriers);

            VK14.vkCmdPipelineBarrier2(buffers.getBuffer(), dependencyInfo);

            this.layout = newLayout;
        }
    }

    public long getHandle() {
        return handle;
    }

    @Override
    public void free() {
        VK14.vkDestroyImageView(this.device.getDevice(), this.handle, null);
    }
}

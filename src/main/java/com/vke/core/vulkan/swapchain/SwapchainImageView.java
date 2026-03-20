package com.vke.core.vulkan.swapchain;

import com.vke.api.abstraction.descriptors.texture.ImageAspect;
import com.vke.api.vulkan.ImageLayout;
import com.vke.core.vulkan.device.LogicalDevice;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.texture.VKTextureUtil;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

public class SwapchainImageView implements Disposable {

    private final long handle;

    private final SwapchainImage parent;
    private final LogicalDevice device;

    private ImageLayout layout = ImageLayout.UNDEFINED;

    public SwapchainImageView(SwapchainImage parent, LogicalDevice device, VkImageViewCreateInfo info) {
        this.parent = parent;
        this.device = device;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageSubresourceRange subresourceRange = VkImageSubresourceRange.calloc(stack)
                    .aspectMask(info.subresourceRange().aspectMask())
                    .baseMipLevel(info.subresourceRange().baseMipLevel())
                    .levelCount(info.subresourceRange().levelCount())
                    .layerCount(info.subresourceRange().layerCount())
                    .baseArrayLayer(info.subresourceRange().baseArrayLayer());

            VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.calloc(stack)
                    .sType$Default()
                    .format(info.format())
                    .subresourceRange(subresourceRange)
                    .viewType(info.viewType())
                    .image(parent.getHandle());

            LongBuffer pImageView = stack.mallocLong(1);
            if (VK14.vkCreateImageView(device.getDevice(), createInfo, null, pImageView) != VK14.VK_SUCCESS) {
                throw new IllegalStateException("Failed to create image view");
            }

            handle = pImageView.get(0);
        }
    }

    public void transitionLayout(VulkanCmdBuffers buffers, ImageLayout newLayout, long srcAccessMask, long dstAccessMask, long srcStageMask, long dstStageMask) {
        VKTextureUtil.transitionLayout(buffers, parent.getHandle(), new ImageAspect(ImageAspect.Bits.COLOR).getVkHandle(), 0, 1, 0, 1,
                layout, newLayout, srcAccessMask, dstAccessMask, srcStageMask, dstStageMask);
        this.layout = newLayout;
    }

    public long getHandle() {
        return handle;
    }

    @Override
    public void free() {
        VK14.vkDestroyImageView(device.getDevice(), this.handle, null);
    }
}

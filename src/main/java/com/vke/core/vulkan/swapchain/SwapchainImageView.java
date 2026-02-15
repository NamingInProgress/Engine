package com.vke.core.vulkan.swapchain;

import com.vke.api.abstraction.IntEnum;
import com.vke.api.abstraction.data.Texture;
import com.vke.api.abstraction.data.TextureView;
import com.vke.api.abstraction.descriptors.texture.TextureFormat;
import com.vke.api.abstraction.descriptors.texture.TextureViewType;
import com.vke.api.vulkan.ImageLayout;
import com.vke.core.rendering.vulkan.device.LogicalDevice;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

public class SwapchainImageView implements TextureView {

    private final SwapchainImage parent;
    private final long handle;
    private final TextureFormat format;
    private ImageLayout layout;
    private final TextureViewType type;

    private final int baseMip, baseLayer, layerCount;

    private final LogicalDevice device;

    public SwapchainImageView(SwapchainImage parent, LogicalDevice device, VkImageViewCreateInfo info) {
        this.parent = parent;
        this.device = device;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pImageView = stack.mallocLong(1);
            if (VK14.vkCreateImageView(device.getDevice(), info, null, pImageView) != VK14.VK_SUCCESS) {
                throw new IllegalStateException("Failed to create image view");
            }
            handle = pImageView.get(0);
            layout = ImageLayout.UNDEFINED;
            format = IntEnum.fromInt(TextureFormat.values(), info.format());
            type = IntEnum.fromInt(TextureViewType.values(), info.viewType());
            baseMip = info.subresourceRange().baseMipLevel();
            baseLayer = info.subresourceRange().baseArrayLayer();
            layerCount = info.subresourceRange().layerCount();
        }
    }

    public void transitionLayout(VulkanCmdBuffers buffers, ImageLayout newLayout, long srcAccessMask, long dstAccessMask, long srcStageMask, long dstStageMask) {
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
                    .image(parent.getHandle())
                    .subresourceRange(range);

            VkDependencyInfo dependencyInfo = VkDependencyInfo.calloc(stack)
                    .sType$Default()
                    .dependencyFlags(0)
                    .pImageMemoryBarriers(barriers);

            VK14.vkCmdPipelineBarrier2(buffers.getBuffer(), dependencyInfo);

            this.layout = newLayout;
        }
    }

    @Override
    public Texture parent() {
        return parent;
    }

    @Override
    public TextureFormat format() {
        return format;
    }

    @Override
    public TextureViewType type() {
        return type;
    }

    @Override
    public ImageLayout layout() {
        return layout;
    }

    @Override
    public int baseMip() {
        return baseMip;
    }

    @Override
    public int mipCount() {
        return 1;
    }

    @Override
    public int baseLayer() {
        return baseLayer;
    }

    @Override
    public int layerCount() {
        return layerCount;
    }

    public long getHandle() {
        return handle;
    }

    @Override
    public void free() {
        VK14.vkDestroyImageView(this.device.getDevice(), this.handle, null);
    }
}

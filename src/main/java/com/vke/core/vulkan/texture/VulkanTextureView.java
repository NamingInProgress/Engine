package com.vke.core.vulkan.texture;

import com.vke.api.abstraction.data.Texture;
import com.vke.api.abstraction.data.TextureView;
import com.vke.api.abstraction.descriptors.texture.ImageAspect;
import com.vke.api.abstraction.descriptors.texture.TextureFormat;
import com.vke.api.abstraction.descriptors.texture.TextureViewType;
import com.vke.api.vulkan.ImageLayout;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.device.LogicalDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

public class VulkanTextureView implements TextureView {

    private final long handle;

    private final Texture parent;
    private final TextureFormat format;
    private final TextureViewType viewType;
    private final ImageAspect aspect;

    private final int baseMip, baseLayer, layerCount;

    private final LogicalDevice device;

    private ImageLayout layout;

    public VulkanTextureView(LogicalDevice device, Description info) {
        this(device, info.parent(), info.format(), info.viewType(),
                info.aspect(), info.baseMip(), info.baseLayer(), info.layerCount());
    }

    protected VulkanTextureView(LogicalDevice device, Texture parent,
                              TextureFormat format, TextureViewType viewType,
                              ImageAspect aspect, int baseMip, int baseLayer, int layerCount) {
        this.device = device;
        this.parent = parent;
        this.format = format;
        this.viewType = viewType;
        this.layout = ImageLayout.UNDEFINED;
        this.baseMip = baseMip;
        this.baseLayer = baseLayer;
        this.layerCount = layerCount;
        this.aspect = aspect;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageSubresourceRange subresourceRange = VkImageSubresourceRange.calloc(stack)
                    .aspectMask(aspect.getVkHandle())
                    .baseMipLevel(baseMip)
                    .levelCount(1)
                    .layerCount(layerCount)
                    .baseArrayLayer(baseLayer);

            VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.calloc(stack)
                    .sType$Default()
                    .format(format.getVkHandle())
                    .subresourceRange(subresourceRange)
                    .viewType(viewType.getVkHandle())
                    .image(parent.getHandle());

            LongBuffer pImageView = stack.mallocLong(1);
            if (VK14.vkCreateImageView(device.getDevice(), createInfo, null, pImageView) != VK14.VK_SUCCESS) {
                throw new IllegalStateException("Failed to create image view");
            }

            handle = pImageView.get(0);
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
        return this.parent;
    }

    @Override
    public TextureFormat format() {
        return this.format;
    }

    @Override
    public TextureViewType type() {
        return this.viewType;
    }

    @Override
    public ImageLayout layout() {
        return this.layout;
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

    public long getHandle() { return handle; }

    @Override
    public void free() {
        VK14.vkDestroyImageView(this.device.getDevice(), this.handle, null);
    }

}

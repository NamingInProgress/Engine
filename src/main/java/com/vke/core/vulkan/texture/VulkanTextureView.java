package com.vke.core.vulkan.texture;

import com.vke.api.rendering.abstraction.enums.texture.ImageAspect;
import com.vke.core.vulkan.device.LogicalDevice;
import com.vke.utils.io.Disposable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

public class VulkanTextureView implements Disposable {

    private final long handle;

    public final VulkanImage image;

    public final int baseMip;
    public final int levelCount;

    public final int baseLayer;
    public final int layerCount;

    public final ImageAspect aspectMask;

    private final LogicalDevice device;

    public VulkanTextureView(LogicalDevice device, VulkanImage parent, VkImageViewCreateInfo info) {
        this.image = parent;
        this.device = device;
        this.baseMip = 0;
        this.levelCount = 0;
        this.baseLayer = 0;
        this.layerCount = 0;
        this.aspectMask = null;

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

    public VulkanTextureView(LogicalDevice device, VulkanImage parent,
                              int baseMip, int levelCount,
                             int baseLayer, int layerCount,
                             ImageAspect aspectMask) {
        this.device = device;
        this.image = parent;
        this.baseMip = baseMip;
        this.levelCount = levelCount;
        this.baseLayer = baseLayer;
        this.layerCount = layerCount;
        this.aspectMask = aspectMask;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageSubresourceRange subresourceRange = VkImageSubresourceRange.calloc(stack)
                    .aspectMask(aspectMask.getVkHandle())
                    .baseMipLevel(baseMip)
                    .levelCount(levelCount)
                    .layerCount(layerCount)
                    .baseArrayLayer(baseLayer);

            VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.calloc(stack)
                    .sType$Default()
                    .format(image.getFormat().getVkHandle())
                    .subresourceRange(subresourceRange)
                    .viewType(image.getType().getVkHandle())
                    .image(parent.getHandle());

            LongBuffer pImageView = stack.mallocLong(1);
            if (VK14.vkCreateImageView(device.getDevice(), createInfo, null, pImageView) != VK14.VK_SUCCESS) {
                throw new IllegalStateException("Failed to create image view");
            }

            handle = pImageView.get(0);
        }
    }

    public long getHandle() { return handle; }

    @Override
    public void free() {
        VK14.vkDestroyImageView(this.device.getDevice(), this.handle, null);
    }

}

package com.vke.core.vulkan.texture;

import com.vke.api.abstraction.data.Texture;
import com.vke.api.abstraction.data.TextureView;
import com.vke.api.abstraction.descriptors.buffer.MemoryUsage;
import com.vke.api.abstraction.descriptors.texture.ImageUsage;
import com.vke.api.abstraction.descriptors.texture.TextureFormat;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.extent.Extent3D;
import com.vke.core.rendering.vulkan.image.VulkanImage;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.extent.VulkanExtentUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

public class VulkanTexture implements Texture {

    private final long VkImage;
    private final VulkanTextureView view;
    private final long VmaAllocation;
    private final Extent3D extent;
    private final TextureFormat VkFormat;

    private final VulkanRenderDevice device;

    public VulkanTexture(VKEngine engine, VulkanRenderDevice device, Description info) {
        this(engine, device, info.format(), info.extent(), info.usageFlags())
    }

    private VulkanTexture(VKEngine engine, VulkanRenderDevice device, TextureFormat VkFormat, Extent3D extent, ImageUsage imageUsageFlags, int VkImageAspectFlags, MemoryUsage memoryUsage) {
        this.device = device;
        this.VkFormat = VkFormat;
        this.extent = extent;

        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkImageCreateInfo imageCreateInfo = getDefaultImageCreateInfo(stack, VkFormat, VkImageUsageFlags, VulkanExtentUtils.createVk3D(stack, extent));
            VmaAllocationCreateInfo allocationCreateInfo = VmaAllocationCreateInfo.calloc(stack)
                    .usage(memoryUsage.getVkHandle())
                    .requiredFlags(VK14.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

            LongBuffer pVkImage = stack.mallocLong(1);
            PointerBuffer pAllocation = stack.mallocPointer(1);
            Vma.vmaCreateImage(setup.getVmaAllocator(), imageCreateInfo, allocationCreateInfo, pVkImage, pAllocation, null);
            this.VkImage = pVkImage.get(0);
            this.VmaAllocation = pAllocation.get(0);

            VkImageViewCreateInfo imageViewCreateInfo = getDefaultImageViewCreateInfo(stack, VkFormat, this, VkImageAspectFlags);
            this.imageView = ImageView.createFromImage(this, engine, setup.getLogicalDevice(), imageViewCreateInfo);
        }
    }

    public static VkImageCreateInfo getDefaultImageCreateInfo(MemoryStack stack, int VkFormat, int VkImageUsageFlags, VkExtent3D extent) {
        return VkImageCreateInfo.calloc(stack)
                .sType$Default()
                .imageType(VK14.VK_IMAGE_TYPE_2D)
                .format(VkFormat)
                .extent(extent)
                .mipLevels(1)
                .arrayLayers(1)
                .samples(VK14.VK_SAMPLE_COUNT_1_BIT)
                .tiling(VK14.VK_IMAGE_TILING_OPTIMAL)
                .usage(VkImageUsageFlags);
    }

    public static VkImageViewCreateInfo getDefaultImageViewCreateInfo(MemoryStack stack, int VkFormat, VulkanImage image, int VkImageAspectFlags) {
        VkImageSubresourceRange subresourceRange = VkImageSubresourceRange.calloc(stack)
                .baseMipLevel(0)
                .levelCount(1)
                .baseArrayLayer(0)
                .layerCount(1)
                .aspectMask(VkImageAspectFlags);

        return VkImageViewCreateInfo.calloc(stack)
                .sType$Default()
                .viewType(VK14.VK_IMAGE_VIEW_TYPE_2D)
                .image(image.VkImage)
                .format(VkFormat)
                .subresourceRange(subresourceRange);
    }

    public long getHandle() { return this.VkImage; }

    @Override
    public void free() {
        imageView.free();
        Vma.vmaDestroyImage(setup.getVmaAllocator(), VkImage, VmaAllocation);
    }

    @Override
    public int width() {
        return 0;
    }

    @Override
    public int height() {
        return 0;
    }

    @Override
    public int depth() {
        return 0;
    }

    @Override
    public TextureFormat format() {
        return null;
    }

    @Override
    public int mipLevels() {
        return 0;
    }

    @Override
    public boolean isSwapchainImage() {
        return false;
    }

    @Override
    public TextureView createView(TextureView.Description info) {
        return null;
    }
}

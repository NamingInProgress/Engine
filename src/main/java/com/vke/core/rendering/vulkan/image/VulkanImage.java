package com.vke.core.rendering.vulkan.image;

import com.vke.core.VKEngine;
import com.vke.core.rendering.vulkan.VulkanSetup;
import com.vke.core.rendering.vulkan.extent.Extent3D;
import com.vke.core.rendering.vulkan.mem.GpuBuffer;
import com.vke.utils.Disposable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

public class VulkanImage implements Disposable {
    private final long VkImage;
    private final ImageView imageView;
    private final long VmaAllocation;
    private final Extent3D extent;
    private final int VkFormat;

    private final VulkanSetup setup;

    public VulkanImage(VKEngine engine, VulkanSetup setup, int VkFormat, Extent3D extent, int VkImageUsageFlags, int VkImageAspectFlags, GpuBuffer.MemoryUsage memoryUsage) {
        this.setup = setup;
        this.VkFormat = VkFormat;
        this.extent = extent;

        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkImageCreateInfo imageCreateInfo = getDefaultImageCreateInfo(stack, VkFormat, VkImageUsageFlags, extent.createVk3D(stack));
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
}

package com.vke.core.vulkan.texture;

import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.abstraction.enums.buffer.MemoryUsage;
import com.vke.api.rendering.abstraction.enums.texture.ImageAspect;
import com.vke.api.rendering.abstraction.enums.texture.Format;
import com.vke.api.rendering.abstraction.enums.texture.TextureType;
import com.vke.api.rendering.vulkan.ImageLayout;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.extent.Extent3D;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.extent.VulkanExtentUtils;
import com.vke.utils.io.Disposable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

public class VulkanImage implements Disposable {

    private final long image;
    private final long allocation;

    protected final Extent3D extent;
    protected final Format format;
    protected final TextureType type;
    protected ImageLayout layout;
    protected final int mipLevels, arrayLayers;
    protected final ImageAspect aspect;

    private VulkanTextureView view;

    private final VulkanRenderDevice device;

    public VulkanImage(long handle, ImageAspect aspect) {
        this.image = handle;
        this.allocation = 0;
        this.extent = null;
        this.format = null;
        this.type = null;
        this.layout = ImageLayout.UNDEFINED;
        this.mipLevels = 1;
        this.arrayLayers = 1;
        this.aspect = aspect;
        this.device = null;
    }

    public VulkanImage(VulkanRenderDevice device, Texture.TextureDesc desc, MemoryUsage memoryUsage) {
        this.device = device;
        this.format = desc.format;
        this.extent = desc.getExtent();
        this.layout = ImageLayout.UNDEFINED;
        this.type = desc.type;
        this.mipLevels = resolveMipLevels(desc);
        this.arrayLayers = resolveArrayLayers(desc);
        this.aspect = desc.aspect;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageCreateInfo imageCreateInfo = getDefaultImageCreateInfo(stack, desc);
            VmaAllocationCreateInfo allocInfo = VmaAllocationCreateInfo.calloc(stack)
                    .usage(memoryUsage.getVkHandle())
                    .requiredFlags(VK14.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

            LongBuffer pImage = stack.mallocLong(1);
            PointerBuffer pAllocation = stack.mallocPointer(1);
            Vma.vmaCreateImage(device.getVmaAllocator(), imageCreateInfo, allocInfo, pImage, pAllocation, null);
            this.image = pImage.get(0);
            this.allocation = pAllocation.get(0);
        }
    }

    public VkImageCreateInfo getDefaultImageCreateInfo(MemoryStack stack, Texture.TextureDesc desc) {
        VkImageCreateInfo info = VkImageCreateInfo.calloc(stack)
                .sType$Default()
                .imageType(desc.type.getVkHandle())
                .format(desc.format.getVkHandle())
                .extent(VulkanExtentUtils.createVk3D(stack, desc.getExtent()))
                .mipLevels(mipLevels)
                .arrayLayers(arrayLayers)
                .samples(VK14.VK_SAMPLE_COUNT_1_BIT)
                .tiling(VK14.VK_IMAGE_TILING_OPTIMAL)
                .usage(desc.usage.getVkHandle());

        if (desc.type == TextureType.TEX_CUBE || desc.type == TextureType.TEX_CUBE_ARRAY) {
            info.flags(VK14.VK_IMAGE_CREATE_CUBE_COMPATIBLE_BIT);
        }
        return info;
    }

    public long getHandle() { return this.image; }

    @Override
    public void free() {
        if (this.allocation == 0) return; // This means it is a swapchain image, and it is not our problem to free
        if (view != null)
            view.free();
        Vma.vmaDestroyImage(device.getVmaAllocator(), image, allocation);
    }

    public void transitionLayout(VulkanCmdBuffers buffers, ImageLayout newLayout) {
        VKTextureUtil.transitionLayout(buffers, this, newLayout);
        this.layout = newLayout;
    }

    public void transitionLayout(VulkanCmdBuffers buffers, ImageLayout newLayout, long srcAccessMask, long dstAccessMask,
                                 long srcStageMask, long dstStageMask) {
        VKTextureUtil.transitionLayout(buffers, this.getHandle(), this.aspect.getVkHandle(), 0, mipLevels,
                0, arrayLayers, this.layout, newLayout, srcAccessMask, dstAccessMask, srcStageMask, dstStageMask);
        this.layout = newLayout;
    }

    public ImageLayout layout() {
        return this.layout;
    }

    public Format getFormat() { return this.format; }

    public TextureType getType() {
        return type;
    }

    public void setView(VulkanTextureView view) { this.view = view; }

    public VulkanTextureView getView() {
        if (view != null) return view;
        view = new VulkanTextureView(device.getLogicalDevice(), this, 0, mipLevels, 0, arrayLayers, aspect);
        return view;
    }

    private static int resolveMipLevels(Texture.TextureDesc desc) {
        return desc.mipLevels == 0 ? 1 : desc.mipLevels;
//        if (desc.mipLevels > 0) {
//            return desc.mipLevels;
//        }
//
//        int maxDim = Math.max(desc.width, Math.max(desc.height, desc.depth));
//        return 1 + (int) Math.floor(Math.log(maxDim) / Math.log(2));
    }

    private static int resolveArrayLayers(Texture.TextureDesc desc) {
        return switch (desc.type) {
            case TEX_CUBE -> 6;
            case TEX_CUBE_ARRAY -> 6 * desc.depth; // or explicit layer count if you add it
            case TEX_1D_ARRAY, TEX_2D_ARRAY -> desc.depth;
            default -> 1;
        };
    }

}

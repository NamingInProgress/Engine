package com.vke.core.vulkan.texture.texture2;

import com.vke.api.rendering.abstraction.data.ImageView;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.vulkan.ImageLayout;
import com.vke.api.rendering.vulkan.memory.VulkanImageBarrier;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.extent.VulkanExtentUtils;
import com.vke.utils.io.Disposable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.function.Function;

public class VulkanTexture implements Texture {

    private final TextureDesc desc;
    private final VulkanRenderDevice device;

    private final long handle;
    private final long allocation;

    private final ArrayList<ImageView> views = new ArrayList<>();

    private ImageView defaultView;
    private ImageLayout layout = ImageLayout.UNDEFINED;

    public VulkanTexture(VulkanRenderDevice device, TextureDesc desc) {
        this.desc = desc;
        this.device = device;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageCreateInfo imageCreateInfo = VkImageCreateInfo.calloc(stack)
                    .sType$Default()
                    .imageType(desc.type.getVkHandle())
                    .format(desc.format.getVkHandle())
                    .extent(VulkanExtentUtils.createVk3D(stack, desc.extent))
                    .mipLevels(desc.mipLevels)
                    .arrayLayers(desc.arrayLayers)
                    .samples(desc.samples.getVkHandle())
                    .tiling(desc.tiling.getVkHandle())
                    .usage(desc.usage.getVkHandle());

            if (desc.cubeMap)
                imageCreateInfo.flags(VK14.VK_IMAGE_CREATE_CUBE_COMPATIBLE_BIT);

            VmaAllocationCreateInfo allocInfo = VmaAllocationCreateInfo.calloc(stack)
                    .usage(desc.memUsage.getVkHandle())
                    .requiredFlags(VK14.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);

            LongBuffer pImage = stack.mallocLong(1);
            PointerBuffer pAllocation = stack.mallocPointer(1);
            Vma.vmaCreateImage(device.getVmaAllocator(), imageCreateInfo, allocInfo, pImage, pAllocation, null);
            this.handle = pImage.get(0);
            this.allocation = pAllocation.get(0);
        }
    }

    @Override
    public ImageView defaultView() {
        if (defaultView != null) return defaultView;
        this.defaultView = new VulkanImageView(device, new ImageView.ImageViewDesc(this, desc.type, format(), 0,
                VK14.VK_REMAINING_MIP_LEVELS, 0, VK14.VK_REMAINING_ARRAY_LAYERS));
        this.views.add(defaultView);
        return defaultView;
    }

    @Override
    public ImageView getView(Function<ImageView.ImageViewDescriptionBuilder, ImageView.ImageViewDesc> consumer) {
        var view = new VulkanImageView(device, consumer.apply(new ImageView.ImageViewDescriptionBuilder(this)));
        this.views.add(view);
        return view;
    }

    // region Transitions
    public void transition(VulkanCmdBuffers cmd, ImageLayout newLayout) {

    }

    public void transition(VulkanCmdBuffers cmd, ImageLayout newLayout, int baseMip, int mipCount) {

    }

    public void transition(VulkanCmdBuffers cmd, ImageLayout newLayout, int baseMip, int mipCount, int baseLayer, int layerCount) {

    }

    public void transition(VulkanCmdBuffers cmd, VulkanImageBarrier barrier) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkImageSubresourceRange range = VkImageSubresourceRange.calloc(stack)
                    .aspectMask(barrier.aspectMask)
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
    // endregion

    @Override
    public TextureDesc description() {
        return desc;
    }

    public long getHandle() {
        return handle;
    }

    @Override
    public void free() {
        views.forEach(Disposable::free);
        Vma.vmaDestroyImage(device.getVmaAllocator(), handle, allocation);
    }
}

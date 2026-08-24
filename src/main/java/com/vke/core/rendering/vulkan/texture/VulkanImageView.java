package com.vke.core.rendering.vulkan.texture;

import com.vke.api.rendering.abstraction.renderer.data.ImageView;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkImageSubresourceRange;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

import java.nio.LongBuffer;
import java.util.Objects;

public class VulkanImageView implements ImageView {

    private final ImageViewDesc desc;
    private final VulkanRenderSystem ctx;

    private final long handle;

    @SuppressWarnings("all")
    public VulkanImageView(VulkanRenderSystem ctx, ImageViewDesc desc) {
        this.desc = desc;
        this.ctx = ctx;

        if (desc.mipCount == -1) desc.mipCount = VK14.VK_REMAINING_MIP_LEVELS;
        if (desc.layerCount == -1) desc.layerCount = VK14.VK_REMAINING_ARRAY_LAYERS;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageSubresourceRange subresourceRange = VkImageSubresourceRange.calloc(stack)
                    .aspectMask(desc.aspect.getIntVal())
                    .baseMipLevel(desc.baseMip)
                    .levelCount(desc.mipCount)
                    .baseArrayLayer(desc.baseLayer)
                    .layerCount(desc.layerCount);

            VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.calloc(stack)
                    .sType$Default()
                    .image(((VulkanTexture) desc.tex).getHandle())
                    .viewType(desc.type.getIntVal())
                    .format(desc.format.getIntVal())
                    .subresourceRange(subresourceRange);

            LongBuffer pImageView = stack.mallocLong(1);
            if (VK14.vkCreateImageView(ctx.device().vkLogicalDevice(), createInfo, null, pImageView) != VK14.VK_SUCCESS) {
                throw new IllegalStateException("Failed to create image view");
            }

            handle = pImageView.get(0);
        }
    }

    @Override
    public ImageViewDesc description() {
        return desc;
    }

    public VulkanTexture parent() {
        return (VulkanTexture) this.desc.tex;
    }

    public long getHandle() { return handle; }

    @Override
    public void free() {
        VK14.vkDestroyImageView(ctx.device().vkLogicalDevice(), this.handle, null);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        VulkanImageView that = (VulkanImageView) o;
        return handle == that.handle && Objects.equals(desc, that.desc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(desc, handle);
    }
}

package com.vke.core.rendering.vulkan.sampler;

import com.vke.api.rendering.abstraction.renderer.data.Sampler;
import com.vke.api.rendering.abstraction.renderer.enums.CompareOp;
import com.vke.api.rendering.abstraction.renderer.enums.Filter;
import com.vke.core.rendering.vulkan.device.VulkanRenderDevice;
import com.vke.core.rendering.vulkan.service.VulkanRenderSystem;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import java.nio.LongBuffer;
import java.util.Objects;

public class VulkanSampler implements Sampler {

    private long handle;

    private final Filter magFilter, minFilter;
    private final CompareOp compareOp;

    private final VulkanRenderDevice device;

    public VulkanSampler(VulkanRenderSystem ctx, Description info) {
        this(ctx, info.magFilter(), info.minFilter(), info.compareOp());
    }

    public VulkanSampler(VulkanRenderSystem ctx, Filter magFilter, Filter minFilter, CompareOp compareOp) {
        this.magFilter = magFilter;
        this.minFilter = minFilter;
        this.compareOp = compareOp;
        this.device = ctx.device();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSamplerCreateInfo info = VkSamplerCreateInfo.calloc(stack)
                    .sType$Default()
                    .magFilter(magFilter.getIntVal())
                    .minFilter(minFilter.getIntVal())
                    .addressModeU(VK14.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                    .addressModeV(VK14.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
                    .borderColor(VK14.VK_BORDER_COLOR_FLOAT_OPAQUE_BLACK);

            if (compareOp != null) {
                info.compareEnable(true);
                info.compareOp(compareOp.getIntVal());
            }

            LongBuffer pSampler = stack.mallocLong(1);
            VK14.vkCreateSampler(device.vkLogicalDevice(), info, null, pSampler);
            this.handle = pSampler.get(0);
        }
    }

    @Override
    public long getHandle() {
        return this.handle;
    }

    @Override
    public void free() {
        VK14.vkDestroySampler(device.vkLogicalDevice(), handle, null);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        VulkanSampler that = (VulkanSampler) o;
        return handle == that.handle;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(handle);
    }
}

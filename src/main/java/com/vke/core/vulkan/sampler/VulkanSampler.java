package com.vke.core.vulkan.sampler;

import com.vke.api.rendering.abstraction.data.Sampler;
import com.vke.api.rendering.abstraction.enums.CompareOp;
import com.vke.api.rendering.abstraction.enums.Filter;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import com.vke.core.vulkan.service.VulkanRenderSystem;
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
                    .magFilter(magFilter.getVkHandle())
                    .minFilter(minFilter.getVkHandle());

            if (compareOp != null) {
                info.compareEnable(true);
                info.compareOp(compareOp.getVkHandle());
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

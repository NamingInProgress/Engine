package com.vke.core.vulkan.sampler;

import com.vke.api.abstraction.data.Sampler;
import com.vke.api.abstraction.descriptors.CompareOp;
import com.vke.api.abstraction.descriptors.Filter;
import com.vke.core.vulkan.device.VulkanRenderDevice;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import java.nio.LongBuffer;

public class VulkanSampler implements Sampler {

    private long handle;

    private final Filter magFilter, minFilter;
    private final CompareOp compareOp;

    private final VulkanRenderDevice device;

    public VulkanSampler(VulkanRenderDevice device, Description info) {
        this(device, info.magFilter(), info.minFilter(), info.compareOp());
    }

    public VulkanSampler(VulkanRenderDevice device, Filter magFilter, Filter minFilter, CompareOp compareOp) {
        this.magFilter = magFilter;
        this.minFilter = minFilter;
        this.compareOp = compareOp;
        this.device = device;

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
            VK14.vkCreateSampler(device.getLogicalDevice().getDevice(), info, null, pSampler);
            this.handle = pSampler.get(0);
        }
    }

    @Override
    public long getHandle() {
        return this.handle;
    }

    @Override
    public void free() {
        VK14.vkDestroySampler(device.getLogicalDevice().getDevice(), handle, null);
    }
}

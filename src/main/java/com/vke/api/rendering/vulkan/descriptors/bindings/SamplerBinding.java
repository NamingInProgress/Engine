package com.vke.api.rendering.vulkan.descriptors.bindings;

import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.core.vulkan.sampler.VulkanSampler;

public class SamplerBinding extends DescriptorBinding {

    public final VulkanSampler[] samplers;

    public SamplerBinding(BindingLayout layout) {
        super(layout);

        this.samplers = new VulkanSampler[layout.descriptorCount];
    }

}

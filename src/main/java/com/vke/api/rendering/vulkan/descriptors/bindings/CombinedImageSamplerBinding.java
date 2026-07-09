package com.vke.api.rendering.vulkan.descriptors.bindings;

import com.vke.api.rendering.vulkan.descriptors.bindings.image.ImageBinding;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.core.vulkan.sampler.VulkanSampler;

public class CombinedImageSamplerBinding extends ImageBinding {

    public final VulkanSampler[] samplers;

    public CombinedImageSamplerBinding(BindingLayout layout) {
        super(layout);

        this.samplers = new VulkanSampler[layout.descriptorCount];
    }
}

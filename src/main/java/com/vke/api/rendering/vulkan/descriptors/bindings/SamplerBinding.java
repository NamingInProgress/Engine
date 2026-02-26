package com.vke.api.rendering.vulkan.descriptors.bindings;

import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;

public class SamplerBinding extends DescriptorBinding {

    public final long[] samplerHandles;

    public SamplerBinding(BindingLayout layout) {
        super(layout);

        this.samplerHandles = new long[layout.descriptorCount];
    }

}

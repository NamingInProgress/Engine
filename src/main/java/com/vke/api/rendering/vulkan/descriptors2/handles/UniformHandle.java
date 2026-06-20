package com.vke.api.rendering.vulkan.descriptors2.handles;

import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.core.vulkan.descriptor.ds2.DescriptorSetInstance;

public class UniformHandle {

    public final int set, binding;
    public final DescriptorType type;
    public final DescriptorSetInstance instance;

    public UniformHandle(DescriptorSetInstance instance, int set, int binding, DescriptorType type) {
        this.set = set;
        this.binding = binding;
        this.type = type;
        this.instance = instance;
    }

}

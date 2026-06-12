package com.vke.api.rendering.vulkan.descriptors2.handles;

import com.vke.api.rendering.vulkan.descriptors.DescriptorType;

public class UniformHandle {

    public final int set, binding;
    public final DescriptorType type;

    public UniformHandle(int set, int binding, DescriptorType type) {
        this.set = set;
        this.binding = binding;
        this.type = type;
    }

}

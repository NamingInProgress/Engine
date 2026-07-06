package com.vke.api.rendering.vulkan.descriptors2.handles;

import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;

public class UniformHandle {

    public final int set, binding;
    public final DescriptorType type;
    public final DescriptorSetGroup group;

    private boolean dirty = false;

    public UniformHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type) {
        this.set = set;
        this.binding = binding;
        this.type = type;
        this.group = group;
    }

    public void setDirty() {
        if (!dirty) dirty = true;
        group.setDirty(this);
    }

}

package com.vke.api.rendering.vulkan.descriptors2.handles;

import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.DescriptorBinding;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;

public class UniformHandle {

    public final int set, binding;
    public final DescriptorType type;
    public final DescriptorSetGroup group;
    public final DescriptorBinding bindingObject;

    private boolean dirty = false;

    public UniformHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, DescriptorBinding bindingObject) {
        this.set = set;
        this.binding = binding;
        this.type = type;
        this.group = group;
        this.bindingObject = bindingObject;
    }

    public void scheduleUpdate() {
        group.scheduleUpdate(this);
    }

    public void setDirty() {
        if (!dirty) dirty = true;
        group.setDirty(this);
    }

}

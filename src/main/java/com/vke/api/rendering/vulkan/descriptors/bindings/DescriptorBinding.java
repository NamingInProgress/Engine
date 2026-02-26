package com.vke.api.rendering.vulkan.descriptors.bindings;

import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;

public abstract class DescriptorBinding {
    public final BindingLayout layout;

    public DescriptorBinding(BindingLayout layout) {
        this.layout = layout;
    }
}

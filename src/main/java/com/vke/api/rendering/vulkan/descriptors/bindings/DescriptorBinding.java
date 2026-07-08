package com.vke.api.rendering.vulkan.descriptors.bindings;

import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;
import com.vke.utils.io.Disposable;

public abstract class DescriptorBinding implements Disposable {
    public final BindingLayout layout;

    public DescriptorBinding(BindingLayout layout) {
        this.layout = layout;
    }

    @Override
    public void free() {}
}

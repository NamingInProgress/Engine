package com.vke.api.rendering.vulkan.descriptors.bindings;

import com.vke.api.rendering.vulkan.descriptors.bindings.image.ImageBinding;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;

public class StorageImageBinding extends ImageBinding {
    public StorageImageBinding(BindingLayout layout) {
        super(layout);
    }
}

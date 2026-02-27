package com.vke.api.rendering.vulkan.descriptors.bindings;

import com.vke.api.rendering.vulkan.descriptors.bindings.image.ImageBinding;
import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;

public class SampledImageBinding extends ImageBinding {
    public SampledImageBinding(BindingLayout layout) {
        super(layout);
    }
}

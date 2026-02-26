package com.vke.api.rendering.vulkan.descriptors.bindings;

import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;

public class SampledImageBinding extends DescriptorBinding {

    public final long[] imageViewHandles;
    public final int[] imageLayouts;

    public SampledImageBinding(BindingLayout layout) {
        super(layout);
        int count = layout.descriptorCount;

        this.imageViewHandles = new long[count];
        this.imageLayouts = new int[count];
    }

}

package com.vke.api.rendering.vulkan.descriptors.bindings;

import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;

public class CombinedImageSamplerBinding extends DescriptorBinding {

    public final long[] imageViewHandles;
    public final long[] samplerHandles;
    public final int[] imageLayouts;

    public CombinedImageSamplerBinding(BindingLayout layout) {
        super(layout);
        int count = layout.descriptorCount;

        this.imageViewHandles = new long[count];
        this.samplerHandles = new long[count];
        this.imageLayouts = new int[count];
    }

}

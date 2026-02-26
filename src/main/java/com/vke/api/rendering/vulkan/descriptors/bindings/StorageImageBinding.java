package com.vke.api.rendering.vulkan.descriptors.bindings;

import com.vke.api.rendering.vulkan.descriptors.info.BindingLayout;

public class StorageImageBinding extends DescriptorBinding {

    public final long[] imageViewHandles;
    public final int[] imageLayouts;

    public StorageImageBinding(BindingLayout layout) {
        super(layout);
        int count = layout.descriptorCount;

        this.imageViewHandles = new long[count];
        this.imageLayouts = new int[count];
    }

}

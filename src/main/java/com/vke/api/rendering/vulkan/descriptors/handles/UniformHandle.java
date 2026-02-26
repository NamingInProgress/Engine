package com.vke.api.rendering.vulkan.descriptors.handles;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.pipeline.DescriptorData;

public class UniformHandle {
    public final long setHandle;
    public final int binding;
    public final PackingType packingType;
    public final DescriptorData.Binding.Type bindingType;

    public UniformHandle(long setHandle, int binding, DescriptorData.Binding.Type bindingType, PackingType packingType) {
        this.setHandle = setHandle;
        this.binding = binding;
        this.bindingType = bindingType;
        this.packingType = packingType;
    }
}

package com.vke.api.rendering.vulkan.descriptors.handles;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import org.jetbrains.annotations.ApiStatus;

public abstract class UniformHandle {
    public final long setHandle;
    public final int binding;
    public final PackingType packingType;
    public final DescriptorType bindingType;

    public UniformHandle(long setHandle, int binding, DescriptorType bindingType, PackingType packingType) {
        this.setHandle = setHandle;
        this.binding = binding;
        this.bindingType = bindingType;
        this.packingType = packingType;
    }

    @ApiStatus.Internal
    public abstract void writeDescriptor(DescriptorWriter writer);
}

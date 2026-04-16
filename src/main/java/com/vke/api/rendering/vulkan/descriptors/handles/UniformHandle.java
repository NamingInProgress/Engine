package com.vke.api.rendering.vulkan.descriptors.handles;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import org.jetbrains.annotations.ApiStatus;

public abstract class UniformHandle {
    public int descriptorSetListIndex;
    public final int binding;
    public final PackingType packingType;
    public final DescriptorType bindingType;
    public final CompiledDescriptorSetLayout compiledLayout;

    public UniformHandle(int descriptorSetListIndex, int binding, DescriptorType bindingType, PackingType packingType, CompiledDescriptorSetLayout compiledLayout) {
        this.descriptorSetListIndex = descriptorSetListIndex;
        this.binding = binding;
        this.bindingType = bindingType;
        this.packingType = packingType;
        this.compiledLayout = compiledLayout;
    }

    @ApiStatus.Internal
    public abstract void writeDescriptor(DescriptorWriter writer, long handle);

    public abstract <T extends UniformHandle> T copy();
}

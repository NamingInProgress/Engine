package com.vke.api.rendering.vulkan.descriptors.handles.single;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;

import java.util.function.Consumer;

public class EntryHandle extends BufferHandle {

    public final long additionalOffset;
    public final long size;

    public EntryHandle(int descriptorSetListIndex, int binding, DescriptorType bindingType, PackingType packingType, CompiledDescriptorSetLayout compiledLayout, int bufferIndex, long bufferSize, long size, long cpuAddress, long gpuAddress, long offset) {
        super(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout, bufferIndex, bufferSize, cpuAddress, gpuAddress);
        this.additionalOffset = offset;
        this.size = size;
    }

    @Override
    public void write(Consumer<BufferSlice> consumer) {
        consumer.accept(new BufferSlice(cpuAddress, offset + additionalOffset, (int) size, packingType));
    }

    @Override
    public <T extends UniformHandle> T copy() {
        return (T) new EntryHandle(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout, bufferIndex, bufferSize, size, cpuAddress, gpuAddress, offset);
    }
}

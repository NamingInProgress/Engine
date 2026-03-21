package com.vke.api.rendering.vulkan.descriptors.handles.single;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;

import java.util.function.Consumer;

public class EntryHandle extends BufferHandle {

    public final long additionalOffset;
    public final long size;

    public EntryHandle(long setHandle, int binding, DescriptorType bindingType, PackingType packingType, int bufferIndex, long bufferSize, long size, long cpuAddress, long gpuAddress, long offset) {
        super(setHandle, binding, bindingType, packingType, bufferIndex, bufferSize, cpuAddress, gpuAddress);
        this.additionalOffset = offset;
        this.size = size;
    }

    @Override
    public void write(Consumer<BufferSlice> consumer) {
        consumer.accept(new BufferSlice(cpuAddress, offset + additionalOffset, (int) size, packingType));
    }

}

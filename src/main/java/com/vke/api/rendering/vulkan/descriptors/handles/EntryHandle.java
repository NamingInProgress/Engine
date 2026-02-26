package com.vke.api.rendering.vulkan.descriptors.handles;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.pipeline.DescriptorData;
import com.vke.core.vulkan.buffers.premade.BufferSlice;

import java.util.function.Consumer;

public class EntryHandle extends BufferHandle {

    public long offset;

    public EntryHandle(long setHandle, int binding, DescriptorData.Binding.Type bindingType, PackingType packingType, int bufferIndex, int size, long cpuAddress, long gpuAddress, long offset) {
        super(setHandle, binding, bindingType, packingType, bufferIndex, size, cpuAddress, gpuAddress);
        this.offset = offset;
    }

    @Override
    public void write(Consumer<BufferSlice> consumer) {
        consumer.accept(new BufferSlice(cpuAddress, offset, size, packingType));
    }

}

package com.vke.api.rendering.vulkan.descriptors.handles;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.pipeline.DescriptorData;
import com.vke.core.vulkan.buffers.premade.BufferSlice;

import java.util.function.Consumer;

public class BufferHandle extends UniformHandle {

    public final int bufferIndex; // -1 if it's a single buffer

    public final long cpuAddress;
    public final long gpuAddress;

    public final int size;

    public BufferHandle(long setHandle, int binding, DescriptorData.Binding.Type bindingType, PackingType packingType, int bufferIndex, int size, long cpuAddress, long gpuAddress) {
        super(setHandle, binding, bindingType, packingType);
        this.bufferIndex = bufferIndex;
        this.size = size;
        this.cpuAddress = cpuAddress;
        this.gpuAddress = gpuAddress;
    }

    public void write(Consumer<BufferSlice> consumer) {
        consumer.accept(new BufferSlice(cpuAddress, 0, size, packingType));
    }

}

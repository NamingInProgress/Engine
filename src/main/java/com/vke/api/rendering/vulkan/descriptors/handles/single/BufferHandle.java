package com.vke.api.rendering.vulkan.descriptors.handles.single;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.pipeline.DescriptorData;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.core.vulkan.buffers.premade.BufferSlice;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

public class BufferHandle extends UniformHandle {

    public final int bufferIndex; // 0 if it's a single buffer

    public final long cpuAddress;
    public final long gpuAddress;

    public final int size;

    // Offset in the big array buffer if it is an array
    public final long offset;

    public BufferHandle(long setHandle, int binding, DescriptorType bindingType, PackingType packingType, int bufferIndex, int size, long cpuAddress, long gpuAddress) {
        super(setHandle, binding, bindingType, packingType);
        this.bufferIndex = bufferIndex;
        this.size = size;
        this.cpuAddress = cpuAddress;
        this.gpuAddress = gpuAddress;

        this.offset = bufferIndex == -1 ? 0 : (long) bufferIndex * size;
    }

    public void write(Consumer<BufferSlice> consumer) {
        consumer.accept(new BufferSlice(cpuAddress, offset, size, packingType));
    }

    @ApiStatus.Internal
    public void writeDescriptor(DescriptorWriter writer) { throw new IllegalStateException("Cannot write a buffer type uniform, how tf did you even get here"); }

}

package com.vke.api.rendering.vulkan.descriptors.handles.array;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.core.vulkan.buffers.premade.BufferSlice;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

public class EntryArrayHandle extends ArrayUniformHandle {

    public final long entrySize;

    public final long cpuAddress;
    public final long gpuAddress;

    public final long offset;

    public final long totalSize;

    public EntryArrayHandle(long setHandle, int binding, DescriptorType bindingType, PackingType packingType, int arraySize, long entrySize, long cpuAddress, long gpuAddress, long offset) {
        super(setHandle, binding, bindingType, packingType, arraySize);
        this.cpuAddress = cpuAddress;
        this.gpuAddress = gpuAddress;
        this.entrySize = entrySize;
        this.offset = offset;

        this.totalSize = arraySize * entrySize;
    }

    public void write(Consumer<BufferSlice> consumer, int index) {
        if ((index + 1) * entrySize >= totalSize) throw new IllegalStateException("This entry does not exist!");
        consumer.accept(new BufferSlice(cpuAddress, offset + (index * entrySize), (int) entrySize, packingType));
    }

    @ApiStatus.Internal
    public void writeDescriptor(DescriptorWriter writer) { throw new IllegalStateException("Cannot write a buffer type uniform, how tf did you even get here"); }

}

package com.vke.api.rendering.vulkan.descriptors.handles.array;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

public class EntryArrayHandle extends ArrayUniformHandle {

    public final long entrySize;

    public final long cpuAddress;
    public final long gpuAddress;

    public final int bufferIndex;
    public final long bufferSize;

    public final long offset;

    public final long totalSize;

    public EntryArrayHandle(int setIdx, int binding, DescriptorType bindingType, PackingType packingType, CompiledDescriptorSetLayout compiledLayout, int bufferIndex, long bufferSize, int arraySize, long entrySize, long cpuAddress, long gpuAddress, long offset) {
        super(setIdx, binding, bindingType, packingType, arraySize, compiledLayout);
        this.cpuAddress = cpuAddress;
        this.gpuAddress = gpuAddress;
        this.entrySize = entrySize;
        this.offset = offset;
        this.bufferIndex = bufferIndex;
        this.bufferSize = bufferSize;

        this.totalSize = bufferIndex * bufferSize + arraySize * entrySize;
    }

    public void write(Consumer<BufferSlice> consumer, int index) {
        if ((index + 1) * entrySize >= totalSize) throw new IllegalStateException("This entry does not exist!");
        consumer.accept(new BufferSlice(cpuAddress, offset + (index * entrySize), (int) entrySize, packingType));
    }

    @ApiStatus.Internal
    public void writeDescriptor(DescriptorWriter writer, long handle) { throw new IllegalStateException("Cannot write a buffer type uniform, how tf did you even get here"); }

    @Override
    public <T extends UniformHandle> T copy() {
        return (T) new EntryArrayHandle(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout, bufferIndex, bufferSize, arraySize, entrySize, cpuAddress, gpuAddress, offset);
    }

}

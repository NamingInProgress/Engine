package com.vke.api.rendering.vulkan.descriptors.handles.single;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;
import com.vke.core.vulkan.descriptor.CompiledDescriptorSetLayout;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

public class BufferHandle extends UniformHandle {

    public final int bufferIndex; // 0 if it's a single buffer

    public final long cpuAddress;
    public final long gpuAddress;

    public final long bufferSize;

    // Offset in the big array buffer if it is an array
    public final long offset;

    public BufferHandle(int setIdx, int binding, DescriptorType bindingType, PackingType packingType, CompiledDescriptorSetLayout compiledLayout, int bufferIndex, long bufferSize, long cpuAddress, long gpuAddress) {
        super(setIdx, binding, bindingType, packingType, compiledLayout);
        this.bufferIndex = bufferIndex;
        this.bufferSize = bufferSize;
        this.cpuAddress = cpuAddress;
        this.gpuAddress = gpuAddress;

        this.offset = (long) bufferIndex * bufferSize;
    }

    public void write(Consumer<BufferSlice> consumer) {
        consumer.accept(new BufferSlice(cpuAddress, offset, (int) bufferSize, packingType));
    }

    @ApiStatus.Internal
    public void writeDescriptor(DescriptorWriter writer, long handle) { throw new IllegalStateException("Cannot write a buffer type uniform, how tf did you even get here"); }

    @Override
    public <T extends UniformHandle> T copy() {
        return (T) new BufferHandle(descriptorSetListIndex, binding, bindingType, packingType, compiledLayout, bufferIndex,  bufferSize, cpuAddress, gpuAddress);
    }

}

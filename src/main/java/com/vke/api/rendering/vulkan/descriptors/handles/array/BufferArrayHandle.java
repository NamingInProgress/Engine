package com.vke.api.rendering.vulkan.descriptors.handles.array;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.pipeline.DescriptorData;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.handles.UniformHandle;
import com.vke.core.vulkan.buffers.premade.BufferSlice;
import com.vke.core.vulkan.descriptor.DescriptorWriter;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Consumer;

public class BufferArrayHandle extends ArrayUniformHandle {

   public final long bufferSize;

   public final long cpuAddress;
   public final long gpuAddress;

   public final long totalSize;

    public BufferArrayHandle(long setHandle, int binding, DescriptorType bindingType, PackingType packingType, int arraySize, long bufferSize, long cpuAddress, long gpuAddress) {
        super(setHandle, binding, bindingType, packingType, arraySize);
        this.bufferSize = bufferSize;
        this.totalSize = (long) arraySize * bufferSize;
        this.cpuAddress = cpuAddress;
        this.gpuAddress = gpuAddress;
    }

    public void write(Consumer<BufferSlice> consumer, int index) {
        if ((long) (index + 1) * bufferSize >= totalSize) throw new IllegalStateException("Requested buffer does not exist!");
        consumer.accept(new BufferSlice(cpuAddress, (long) index * bufferSize, (int) bufferSize, packingType));
    }

    @ApiStatus.Internal
    public void writeDescriptor(DescriptorWriter writer) { throw new IllegalStateException("Cannot write a buffer type uniform, how tf did you even get here"); }

}

package com.vke.api.rendering.vulkan.descriptors.handles;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.pipeline.DescriptorData;
import com.vke.core.vulkan.buffers.premade.BufferSlice;

import java.util.function.Consumer;

public class BufferArrayHandle extends UniformHandle {

   public final int bufferCount;
   public final int bufferSize;

   public final long cpuAddress;
   public final long gpuAddress;

   public final int totalSize;

    public BufferArrayHandle(long setHandle, int binding, DescriptorData.Binding.Type bindingType, PackingType packingType, int bufferCount, int bufferSize, int totalSize, long cpuAddress, long gpuAddress) {
        super(setHandle, binding, bindingType, packingType);
        this.bufferCount = bufferCount;
        this.bufferSize = bufferSize;
        this.totalSize = totalSize;
        this.cpuAddress = cpuAddress;
        this.gpuAddress = gpuAddress;
    }

    public void write(Consumer<BufferSlice> consumer, int index) {
        if ((index + 1) * bufferSize >= totalSize) throw new IllegalStateException("Requested buffer does not exist!");
        consumer.accept(new BufferSlice(cpuAddress, (long) index * bufferSize, bufferSize, packingType));
    }

}

package com.vke.api.rendering.vulkan.descriptors2.handles.buf;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;
import com.vke.core.vulkan.descriptor.ds2.DescriptorSetInstance;

import java.util.function.Consumer;

public class BufferHandle extends UniformHandle {

    public final int arrayIndex;

    public final long bufferSize;
    public final long offset;
    public final long cpuAddress, gpuAddress;

    public BufferHandle(DescriptorSetInstance instance, int set, int binding, DescriptorType type, long bufferSize, long cpuAddress) {
        this(instance, set, binding, type, 0, bufferSize, cpuAddress, 0);
    }

    public BufferHandle(DescriptorSetInstance instance, int set, int binding, DescriptorType type, long bufferSize, long cpuAddress, long gpuAddress) {
        this(instance, set, binding, type, 0, bufferSize, cpuAddress, gpuAddress);
    }

    public BufferHandle(DescriptorSetInstance instance, int set, int binding, DescriptorType type, int arrayIndex, long bufferSize, long cpuAddress, long gpuAddress) {
        super(instance, set, binding, type);
        this.arrayIndex = arrayIndex;
        this.bufferSize = bufferSize;
        this.offset = arrayIndex * bufferSize;
        this.cpuAddress = cpuAddress;
        this.gpuAddress = gpuAddress;
    }

    public void write(Consumer<BufferSlice> writer) {
        writer.accept(new BufferSlice(cpuAddress, offset, (int) this.bufferSize, PackingType.fromDescriptorType(type)));
    }

}

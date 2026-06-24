package com.vke.api.rendering.vulkan.descriptors2.handles.buf;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors2.util.MultiWriteCounter;
import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;
import com.vke.core.vulkan.descriptor.ds2.DescriptorSetInstance;

import java.util.function.Consumer;

public class MultiWriteBufferHandle extends BufferHandle {

    private final MultiWriteCounter counter;
    private final long singleBufferSize;

    public MultiWriteBufferHandle(DescriptorSetInstance instance, int set, int binding, DescriptorType type, int maxRotations, long fullBufferSize, long singleBufferSize, long cpuAddress) {
        this(instance, set, binding, type, maxRotations, fullBufferSize, singleBufferSize, cpuAddress, 0);
    }

    public MultiWriteBufferHandle(DescriptorSetInstance instance, int set, int binding, DescriptorType type, int maxRotations, long fullBufferSize, long singleBufferSize, long cpuAddress, long gpuAddress) {
        super(instance, set, binding, type, 0, fullBufferSize, cpuAddress, gpuAddress);
        this.counter = new MultiWriteCounter(maxRotations);
        this.singleBufferSize = singleBufferSize;
    }

    public void advance() {
        this.counter.advance();
    }

    public void reset() {
        this.counter.reset();
    }

    public long getCurrentOffset() { return this.singleBufferSize * this.counter.getCurrentRotation(); }

    @Override
    public void write(Consumer<BufferSlice> writer) {
        writer.accept(new BufferSlice(cpuAddress, getCurrentOffset(),
                (int) singleBufferSize, PackingType.fromDescriptorType(type)));
    }

}

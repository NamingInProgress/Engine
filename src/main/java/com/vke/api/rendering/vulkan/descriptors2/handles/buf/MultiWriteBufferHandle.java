package com.vke.api.rendering.vulkan.descriptors2.handles.buf;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.BufferBinding;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.util.MultiWriteCounter;
import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;

import java.util.function.Consumer;

public class MultiWriteBufferHandle extends BufferHandle {

    private final MultiWriteCounter counter;
    private final long singleBufferSize;

    public MultiWriteBufferHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, BufferBinding bufBinding, FrameCounter fc,
                                  int maxRotations, long fullBufferSize, long singleBufferSize, long cpuAddress) {
        this(group, set, binding, type, bufBinding, fc,
                maxRotations, fullBufferSize, singleBufferSize, cpuAddress, 0);
    }

    public MultiWriteBufferHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, BufferBinding bufBinding, FrameCounter fc,
                                  int maxRotations, long fullBufferSize, long singleBufferSize, long cpuAddress, long gpuAddress) {
        super(group, set, binding, type, bufBinding, fc, 0, fullBufferSize, cpuAddress, gpuAddress);
        this.counter = new MultiWriteCounter(maxRotations);
        this.singleBufferSize = singleBufferSize;
    }

    public void advance() {
        this.counter.advance();
    }

    public void reset() {
        this.counter.reset();
    }

    @Override
    public long getOffset() { return super.getOffset() + this.singleBufferSize * this.counter.getCurrentRotation(); }

    @Override
    public void write(Consumer<BufferSlice> writer) {
        writer.accept(new BufferSlice(cpuAddress, getOffset(),
                (int) singleBufferSize, PackingType.fromDescriptorType(type)));
    }

}

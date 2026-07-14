package com.vke.api.rendering.vulkan.descriptors2.handles.buf;

import com.vke.api.rendering.abstraction.renderer.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.BufferBinding;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.util.MultiWriteCounter;
import com.vke.core.rendering.vulkan.buffers.MappedGpuRingBuffer;
import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;

import java.util.function.Consumer;

public class MultiWriteBufferHandle extends BufferHandle {

    private MultiWriteCounter counter;
    private final long singleBufferSize;

    public MultiWriteBufferHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, BufferBinding bufBinding,
                                  int maxRotations, long fullBufferSize, long singleBufferSize, long cpuAddress, long gpuAddress) {
        super(group, set, binding, type, bufBinding, 0, fullBufferSize, cpuAddress, gpuAddress);
        this.counter = new MultiWriteCounter(maxRotations);
        this.singleBufferSize = singleBufferSize;
    }

    public void advance() {
        if (!this.counter.advance()) {
            this.grow();
            this.counter = new MultiWriteCounter(this.counter.getMaxRotations() * 2);
        }
        ((MappedGpuRingBuffer) bufBinding.buffer).rotate();
    }

    public void reset() {
        this.counter.reset();
    }

    @Override
    public void nextFrame() {
        reset();
        ((MappedGpuRingBuffer) this.bufBinding.buffer).rotate();
    }

    @Override
    public void write(Consumer<BufferSlice> writer) {
        writer.accept(new BufferSlice(cpuAddress, getOffset(),
                (int) singleBufferSize, PackingType.fromDescriptorType(type)));
    }

}

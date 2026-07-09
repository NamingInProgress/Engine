package com.vke.api.rendering.vulkan.descriptors2.handles.buf;

import com.vke.api.rendering.FrameCounter;
import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.BufferBinding;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.core.vulkan.buffers.MappedGpuRingBuffer;
import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;

import java.util.function.Consumer;

public class BufferHandle extends UniformHandle {

    public final BufferBinding bufBinding;

    public final int arrayIndex;

    public final long bufferSize;
    public final long offset;
    public final long cpuAddress, gpuAddress;

    private final FrameCounter fc;

    public BufferHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, BufferBinding bufBinding, FrameCounter fc,
                        long bufferSize, long cpuAddress) {
        this(group, set, binding, type, bufBinding, fc, 0, bufferSize, cpuAddress, 0);
    }

    public BufferHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, BufferBinding bufBinding, FrameCounter fc,
                        long bufferSize, long cpuAddress, long gpuAddress) {
        this(group, set, binding, type, bufBinding, fc, 0, bufferSize, cpuAddress, gpuAddress);
    }

    public BufferHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, BufferBinding bufBinding, FrameCounter fc,
                        int arrayIndex, long bufferSize, long cpuAddress, long gpuAddress) {
        super(group, set, binding, type, bufBinding);
        this.arrayIndex = arrayIndex;
        this.bufferSize = bufferSize;
        this.offset = arrayIndex * bufferSize;
        this.cpuAddress = cpuAddress;
        this.gpuAddress = gpuAddress;
        this.bufBinding = bufBinding;
        this.fc = fc;
    }

    public void nextFrame() {
        if (this.bufBinding.buffer instanceof MappedGpuRingBuffer buf)
            buf.rotate();
    }

    public long getOffset() {
        return bufBinding.singleBufferSize * fc.currentIndex() + offset;
    }

    public void write(Consumer<BufferSlice> writer) {
        writer.accept(new BufferSlice(cpuAddress, getOffset(), (int) this.bufferSize, PackingType.fromDescriptorType(type)));
    }

}

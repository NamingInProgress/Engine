package com.vke.api.rendering.vulkan.descriptors2.handles.buf;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors.bindings.BufferBinding;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.core.rendering.vulkan.buffers.MappedGpuRingBuffer;
import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;
import com.vke.core.rendering.vulkan.descriptor.ds2.DescriptorSetInstance;

import java.util.function.Consumer;

public class BufferHandle extends UniformHandle {

    public final BufferBinding bufBinding;

    public final int arrayIndex;

    public long bufferSize;
    public final long offset;
    public long cpuAddress, gpuAddress;

    public BufferHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, BufferBinding bufBinding,
                        long bufferSize, long cpuAddress, long gpuAddress) {
        this(group, set, binding, type, bufBinding, 0, bufferSize, cpuAddress, gpuAddress);
    }

    public BufferHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, BufferBinding bufBinding,
                        int arrayIndex, long bufferSize, long cpuAddress, long gpuAddress) {
        super(group, set, binding, type, bufBinding);
        this.arrayIndex = arrayIndex;
        this.bufferSize = bufferSize;
        this.offset = arrayIndex * bufferSize;
        this.cpuAddress = cpuAddress;
        this.gpuAddress = gpuAddress;
        this.bufBinding = bufBinding;
    }

    public void nextFrame() {
        this.bufBinding.nextFrame();
    }

    public long getOffset() {
        if (bufBinding.buffer instanceof MappedGpuRingBuffer rb) {
            return rb.getOffset() + offset;
        }
        return 0;
    }

    public void grow() {
        this.bufBinding.grow();
        this.useNewBuffer();
    }

    private void useNewBuffer() {
        var newBuffer = DescriptorSetInstance.generateBuffer(group.getRenderSystem(), this.bufBinding.layout);
        this.bufferSize = newBuffer.getSize();
        this.cpuAddress = newBuffer.getMappedAddress();
        this.gpuAddress = newBuffer.getGpuBuffer().getBuffer();
        this.bufBinding.setBuffer(newBuffer);
        this.scheduleUpdate();
    }

    public void growRuntimeSizeArray(int newElementCount) {
        this.bufBinding.layout.resizeRSA(newElementCount);
        this.useNewBuffer();
    }

    public void write(Consumer<BufferSlice> writer) {
        writer.accept(new BufferSlice(cpuAddress, getOffset(), (int) this.bufferSize, PackingType.fromDescriptorType(type)));
    }

}

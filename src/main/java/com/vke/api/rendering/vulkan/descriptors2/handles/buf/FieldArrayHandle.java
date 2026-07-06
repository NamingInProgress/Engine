package com.vke.api.rendering.vulkan.descriptors2.handles.buf;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;

import java.util.function.Consumer;

public class FieldArrayHandle extends UniformHandle {

    public final BufferHandle parent;

    public final long fieldOffset;
    public final int stride;
    public final int elementCount;
    public final int totalLength;

    public FieldArrayHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, BufferHandle parent,
                            long fieldOffset, int stride, int elementCount) {
        super(group, set, binding, type);
        this.parent = parent;
        this.fieldOffset = fieldOffset;
        this.stride = stride;
        this.elementCount = elementCount;
        this.totalLength = elementCount * stride;
    }

    public void write(int index, Consumer<BufferSlice> writer) {
        if (index >= elementCount) throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + elementCount);
        writer.accept(new BufferSlice(parent.cpuAddress, parent.offset + fieldOffset + (long) index * stride,
                this.stride, PackingType.fromDescriptorType(type)));
    }

    public void write(Consumer<BufferSlice> writer) {
        writer.accept(new BufferSlice(parent.cpuAddress, parent.offset + fieldOffset,
                this.totalLength, PackingType.fromDescriptorType(type)));
    }

}

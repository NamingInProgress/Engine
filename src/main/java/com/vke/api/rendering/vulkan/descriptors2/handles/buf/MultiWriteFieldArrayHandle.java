package com.vke.api.rendering.vulkan.descriptors2.handles.buf;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;

import java.util.function.Consumer;

public class MultiWriteFieldArrayHandle extends FieldArrayHandle {

    public final MultiWriteBufferHandle parent;

    public MultiWriteFieldArrayHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, MultiWriteBufferHandle parent, long fieldOffset, int stride, int elementCount) {
        super(group, set, binding, type, parent, fieldOffset, stride, elementCount);
        this.parent = parent;
    }

    public void advanceBuffer() {
        this.parent.advance();
    }

    public void resetBuffer() {
        this.parent.reset();
    }

    @Override
    public void write(int index, Consumer<BufferSlice> writer) {
        if (index >= this.elementCount) throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + elementCount);
        writer.accept(new BufferSlice(this.parent.cpuAddress, this.parent.getOffset() + fieldOffset + (long) index * this.stride,
                this.stride, PackingType.fromDescriptorType(type)));
    }

    @Override
    public void write(Consumer<BufferSlice> writer) {
        writer.accept(new BufferSlice(this.parent.cpuAddress, this.parent.getOffset() + fieldOffset,
                this.totalLength, PackingType.fromDescriptorType(type)));
    }

}

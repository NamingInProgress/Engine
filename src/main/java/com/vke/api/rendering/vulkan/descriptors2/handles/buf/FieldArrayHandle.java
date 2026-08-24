package com.vke.api.rendering.vulkan.descriptors2.handles.buf;

import com.vke.api.rendering.abstraction.renderer.enums.buffer.PackingType;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.BufferResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;

import java.util.function.Consumer;

public class FieldArrayHandle extends UniformHandle implements FieldArrayResource {

    public final BufferHandle parent;

    public final long fieldOffset;
    public final int stride;
    public final int elementCount;
    public final int totalLength;

    public FieldArrayHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, BufferHandle parent,
                            long fieldOffset, int stride, int elementCount) {
        super(group, set, binding, type, parent.bindingObject);
        this.parent = parent;
        this.fieldOffset = fieldOffset;
        this.stride = stride;
        this.elementCount = elementCount;
        this.totalLength = elementCount * stride;
    }

    @Override
    public void write(int index, Consumer<BufferSlice> writer) {
        if (index >= this.elementCount) {
            if (parent.bufBinding.layout.type == DescriptorType.STORAGE_BUFFER || parent.bufBinding.layout.type == DescriptorType.STORAGE_BUFFER_DYNAMIC) {
                int newCount = this.elementCount;

                while (newCount <= index) {
                    newCount *= 2;
                }

                parent.growRuntimeSizeArray(newCount);
            } else {
                throw new IndexOutOfBoundsException("Index " + index + " out of bounds for length " + elementCount);
            }
        }
        writer.accept(new BufferSlice(parent.cpuAddress, parent.getOffset() + fieldOffset + (long) index * stride,
                this.stride, PackingType.fromDescriptorType(type)));
    }

    @Override
    public void write(Consumer<BufferSlice> writer) {
        writer.accept(new BufferSlice(parent.cpuAddress, parent.getOffset() + fieldOffset,
                this.totalLength, PackingType.fromDescriptorType(type)));
    }

    @Override
    public BufferResource parent() {
        return this.parent;
    }

    @Override
    public void nextWrite() {
        this.parent.nextWrite();
    }
}

package com.vke.api.rendering.vulkan.descriptors2.handles.buf;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors2.handles.UniformHandle;
import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;
import com.vke.core.vulkan.descriptor.ds2.DescriptorSetInstance;

import java.util.function.Consumer;

public class FieldHandle extends UniformHandle {

    public final BufferHandle parent;

    public final long fieldOffset;
    public final int fieldLength;

    public FieldHandle(DescriptorSetInstance instance, int set, int binding, DescriptorType type, BufferHandle parent, long fieldOffset, int fieldLength) {
        super(instance, set, binding, type);
        this.parent = parent;
        this.fieldLength = fieldLength;
        this.fieldOffset = fieldOffset;
    }

    public void write(Consumer<BufferSlice> writer) {
        writer.accept(new BufferSlice(parent.cpuAddress, parent.offset + fieldOffset,
                fieldLength, PackingType.fromDescriptorType(type)));
    }

}

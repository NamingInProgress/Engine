package com.vke.api.rendering.vulkan.descriptors2.handles.buf;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;
import com.vke.core.vulkan.descriptor.ds2.DescriptorSetInstance;

import java.util.function.Consumer;

public class MultiWriteFieldHandle extends FieldHandle {

    private final MultiWriteBufferHandle parent;

    public MultiWriteFieldHandle(DescriptorSetInstance instance, int set, int binding, DescriptorType type, MultiWriteBufferHandle parent, long fieldOffset, int fieldLength) {
        super(instance, set, binding, type, parent, fieldOffset, fieldLength);
        this.parent = parent;
    }

    public void advanceBuffer() {
        this.parent.advance();
    }

    public void resetBuffer() {
        this.parent.reset();
    }

    @Override
    public void write(Consumer<BufferSlice> writer) {
        writer.accept(new BufferSlice(this.parent.cpuAddress, this.parent.getCurrentOffset() + fieldOffset,
                fieldLength, PackingType.fromDescriptorType(type)));
    }

}

package com.vke.api.rendering.vulkan.descriptors2.handles.buf;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;

import java.util.function.Consumer;

public class MultiWriteFieldHandle extends FieldHandle {

    private final MultiWriteBufferHandle parent;

    public MultiWriteFieldHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, MultiWriteBufferHandle parent, long fieldOffset, int fieldLength) {
        super(group, set, binding, type, parent, fieldOffset, fieldLength);
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
        writer.accept(new BufferSlice(this.parent.cpuAddress, this.parent.getOffset() + fieldOffset,
                fieldLength, PackingType.fromDescriptorType(type)));
    }

}

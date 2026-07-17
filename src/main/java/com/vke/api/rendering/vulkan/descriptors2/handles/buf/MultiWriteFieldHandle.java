package com.vke.api.rendering.vulkan.descriptors2.handles.buf;

import com.vke.api.rendering.abstraction.renderer.enums.buffer.PackingType;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.BufferResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.MultiWriteBufferResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.MultiWriteFieldResource;
import com.vke.api.rendering.vulkan.descriptors.DescriptorType;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;

import java.util.function.Consumer;

public class MultiWriteFieldHandle extends FieldHandle implements MultiWriteFieldResource {

    private final MultiWriteBufferHandle parent;

    public MultiWriteFieldHandle(DescriptorSetGroup group, int set, int binding, DescriptorType type, MultiWriteBufferHandle parent, long fieldOffset, int fieldLength) {
        super(group, set, binding, type, parent, fieldOffset, fieldLength);
        this.parent = parent;
    }

    @Override
    public void nextWrite() {
        this.parent.nextWrite();
    }

    @Override
    public void reset() {
        this.parent.reset();
    }

    @Override
    public void write(Consumer<BufferSlice> writer) {
        writer.accept(new BufferSlice(this.parent.cpuAddress, this.parent.getOffset() + fieldOffset,
                fieldLength, PackingType.fromDescriptorType(type)));
    }

    @Override
    public MultiWriteBufferResource parent() {
        return this.parent;
    }
}

package com.vke.api.pipeline.handles;

import com.vke.api.rendering.abstraction.enums.buffer.PackingType;
import com.vke.core.vulkan.buffers.premade.slice.BufferSlice;

import java.util.function.Consumer;

public class PushConstantHandle extends DataHandle {

    public int size;
    public long offset;
    public long buffer;
    public PackingType packing;

    public void write(Consumer<BufferSlice> consumer) {
        consumer.accept(new BufferSlice(buffer, offset, size, packing));
    }

}

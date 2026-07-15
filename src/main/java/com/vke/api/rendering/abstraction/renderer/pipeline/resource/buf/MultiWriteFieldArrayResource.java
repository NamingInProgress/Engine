package com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf;

import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;

import java.util.function.Consumer;

public interface MultiWriteFieldArrayResource extends FieldArrayResource {
    void reset();
    MultiWriteBufferResource parent();

    void write(int index, Consumer<BufferSlice> writer);
    void write(Consumer<BufferSlice> writer);
}

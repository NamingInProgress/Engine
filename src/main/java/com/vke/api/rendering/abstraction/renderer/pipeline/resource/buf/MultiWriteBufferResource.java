package com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf;

import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;

import java.util.function.Consumer;

public interface MultiWriteBufferResource extends BufferResource {
    void reset();
    void write(Consumer<BufferSlice> writer);
}

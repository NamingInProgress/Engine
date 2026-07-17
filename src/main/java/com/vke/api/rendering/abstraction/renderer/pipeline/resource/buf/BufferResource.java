package com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf;

import com.vke.api.rendering.abstraction.renderer.pipeline.resource.ShaderResource;
import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;

import java.util.function.Consumer;

public interface BufferResource extends ShaderResource {

    void write(Consumer<BufferSlice> writer);
    long getOffset();

}

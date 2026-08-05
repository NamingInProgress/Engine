package com.vke.api.rendering.pbr;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;

import java.io.IOException;

public abstract class MaterialLayer {

    public abstract void putSelf(RenderSystem sys, BufferSlice encoder) throws IOException;

    public abstract int hashCode();
    public abstract boolean equals(Object obj);
}

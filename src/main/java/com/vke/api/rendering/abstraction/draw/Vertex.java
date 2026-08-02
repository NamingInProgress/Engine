package com.vke.api.rendering.abstraction.draw;

import com.vke.api.rendering.abstraction.renderer.data.TexturableEncoder;

public interface Vertex {
    default int getByteStride() { throw new RuntimeException("Stub!"); }
    default void putSelf(TexturableEncoder buf) { throw new RuntimeException("Stub!"); }
}
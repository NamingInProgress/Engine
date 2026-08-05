package com.vke.api.rendering.abstraction.draw;

import com.vke.api.rendering.abstraction.renderer.data.RenderingEncoder;

public interface Vertex {
    default int getByteStride() { throw new RuntimeException("Stub!"); }
    default void putSelf(RenderingEncoder buf) { throw new RuntimeException("Stub!"); }
}
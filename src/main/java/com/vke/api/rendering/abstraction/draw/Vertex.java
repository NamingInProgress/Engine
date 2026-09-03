package com.vke.api.rendering.abstraction.draw;

import com.vke.api.rendering.abstraction.renderer.data.RenderingEncoder;

public interface Vertex extends SelfPuttable {
    default int getByteStride() { throw new RuntimeException("Stub!"); }

    @Override
    default void putSelf(RenderingEncoder buf) { throw new RuntimeException("Stub!"); }
}
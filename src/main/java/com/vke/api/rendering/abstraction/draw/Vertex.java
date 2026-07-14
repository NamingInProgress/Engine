package com.vke.api.rendering.abstraction.draw;

import com.vke.api.rendering.abstraction.renderer.data.VertexEncoder;

public interface Vertex {
    int getByteStride();
    void putSelf(VertexEncoder buf);
}
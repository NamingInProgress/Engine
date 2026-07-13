package com.vke.api.draw;

import com.vke.api.rendering.abstraction.data.VertexEncoder;

public interface Vertex {
    int getByteStride();
    void putSelf(VertexEncoder buf);
}
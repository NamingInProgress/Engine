package com.vke.api.draw;

import com.vke.api.rendering.vulkan.buffer.VertexByteSink;

public interface Vertex {

    int getByteStride();
    void putSelf(VertexByteSink buf);

}
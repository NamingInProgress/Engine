package com.vke.api.rendering.vulkan.buffer;

import java.nio.ByteBuffer;

public interface Vertex {

    int getByteStride();
    void putSelf(ByteSink buf);

}

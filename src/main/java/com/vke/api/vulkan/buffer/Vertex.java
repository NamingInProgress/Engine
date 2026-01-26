package com.vke.api.vulkan.buffer;

import java.nio.ByteBuffer;

public interface Vertex {

    int getByteStride();
    void putSelf(ByteBuffer buf);

}

package com.vke.api.rendering.abstraction.draw;

import com.vke.api.rendering.abstraction.renderer.data.TexturableEncoder;

public interface Vertex {
    int TEX_SIZE = 4;

    int getByteStride();
    void putSelf(TexturableEncoder buf);
}
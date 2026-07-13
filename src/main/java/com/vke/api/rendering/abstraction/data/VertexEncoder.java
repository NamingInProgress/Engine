package com.vke.api.rendering.abstraction.data;

import org.jetbrains.annotations.Nullable;
import org.joml.*;

public interface VertexEncoder extends ByteEncoder {
    void sampler2D(@Nullable Texture texture);
}

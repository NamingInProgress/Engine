package com.vke.api.rendering.abstraction.renderer.data;

import org.jetbrains.annotations.Nullable;

public interface TexturableEncoder extends ByteEncoder {
    void sampler2D(@Nullable Texture texture);
}

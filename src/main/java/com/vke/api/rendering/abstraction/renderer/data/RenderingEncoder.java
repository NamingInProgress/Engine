package com.vke.api.rendering.abstraction.renderer.data;

import com.vke.api.rendering.pbr.Material;
import org.jetbrains.annotations.Nullable;

public interface RenderingEncoder extends ByteEncoder {
    void sampler2D(@Nullable Texture texture);
    void material(@Nullable Material material);
}

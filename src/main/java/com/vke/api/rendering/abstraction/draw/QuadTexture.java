package com.vke.api.rendering.abstraction.draw;

import com.vke.api.rendering.abstraction.renderer.data.Texture;

public interface QuadTexture {
    float[] uvFor(); // u, v, texWidth (uv space), texHeight (uv space)
    Texture texture();
}

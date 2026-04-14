package com.vke.api.draw;

import com.vke.api.rendering.abstraction.data.Texture;

public interface QuadTexture {
    float[] uvFor(); // u, v, texWidth (uv space), texHeight (uv space)
    Texture texture();
}

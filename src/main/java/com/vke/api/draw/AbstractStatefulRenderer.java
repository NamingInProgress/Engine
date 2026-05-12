package com.vke.api.draw;

import com.vke.api.rendering.abstraction.data.Texture;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractStatefulRenderer implements Drawable {
    protected float r,g,b,a;
    protected QuadTexture texture;
    protected Texture t;
    protected float z;

    public void color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void texture(@Nullable QuadTexture texture) {
        this.texture = texture;
        if (texture != null) {
            this.t = texture.texture();
        } else {
            this.t = null;
        }
    }

    public void z(float z) {
        this.z = z;
    }
}

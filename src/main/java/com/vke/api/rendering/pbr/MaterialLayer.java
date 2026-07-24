package com.vke.api.rendering.pbr;

import com.vke.api.rendering.abstraction.renderer.data.TexturableEncoder;

public abstract class MaterialLayer {

    public abstract void putSelf(TexturableEncoder encoder);

    public abstract int hashCode();
    public abstract boolean equals(Object obj);
}

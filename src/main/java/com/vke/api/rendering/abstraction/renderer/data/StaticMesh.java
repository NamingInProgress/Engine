package com.vke.api.rendering.abstraction.renderer.data;

import com.vke.api.rendering.abstraction.draw.Drawable;

public interface StaticMesh extends Drawable {
    int key();
    int hashCode();
    boolean equals(Object o);
}

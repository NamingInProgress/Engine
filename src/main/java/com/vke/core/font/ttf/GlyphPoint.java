package com.vke.core.font.ttf;

import org.joml.Vector2f;

public class GlyphPoint {

    public final Vector2f vec;
    public final boolean onCurve;

    public GlyphPoint(Vector2f vec, boolean onCurve) {
        this.vec = vec;
        this.onCurve = onCurve;
    }
}

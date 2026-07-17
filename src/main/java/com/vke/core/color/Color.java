package com.vke.core.color;

import org.joml.Vector4f;

public class Color extends Vector4f {
    public static final Color VKE = new Color(0.2f, 0.3f, 0.3f, 0.1f);
    public static final Color RED = new Color(1, 0, 0, 1);
    public static final Color GREEN = new Color(0, 1, 0, 1);
    public static final Color BLUE = new Color(0, 0, 1, 1);
    public static final Color BLACK = new Color(0, 0, 0, 1);
    public static final Color WHITE = new Color(1, 1, 1, 1);

    public Color() {
        this(WHITE);
    }

    public Color(float r, float g, float b, float a) {
        super(r, g, b, a);
    }

    public Color(Color toCopy) {
        this(toCopy.x, toCopy.y, toCopy.z, toCopy.w);
    }

    public float r() {
        return x;
    }

    public float g() {
        return y;
    }

    public float b() {
        return z;
    }

    public float a() {
        return w;
    }
}

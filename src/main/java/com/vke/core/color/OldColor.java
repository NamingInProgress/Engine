package com.vke.core.color;

import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;
import org.joml.Vector4f;

public class OldColor extends Vector4f {
    public static final OldColor VKE = new OldColor(0.2f, 0.3f, 0.3f, 1.0f);
    public static final OldColor RED = new OldColor(1, 0, 0, 1);
    public static final OldColor GREEN = new OldColor(0, 1, 0, 1);
    public static final OldColor BLUE = new OldColor(0, 0, 1, 1);
    public static final OldColor BLACK = new OldColor(0, 0, 0, 1);
    public static final OldColor WHITE = new OldColor(1, 1, 1, 1);

    public OldColor() {
        this(WHITE);
    }

    public OldColor(float r, float g, float b, float a) {
        super(r, g, b, a);
    }

    public OldColor(OldColor toCopy) {
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

    public void putSelf(BufferSlice encoder) {
        encoder.putFloat4(x, y, z, w);
    }

    public float[] toFloat() {
        return new float[]{ x, y, z, w };
    }

    public static OldColor parse(String str) {
        if (str == null) return OldColor.BLACK;
        return OldColor.WHITE; // TODO: v22 will replace :thumbsup:
    }

}

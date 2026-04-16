package com.vke.core.spline;

public class SplineCommand {
    private final Type type;
    private final float[] data;

    public SplineCommand(Type type, float[] data) {
        this.type = type;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public float[] getData() {
        return data;
    }

    public enum Type {
        MoveTo,
        LineTo,
        QuadTo,
        CubicTo,
        Close
    }
}

package com.vke.core.rendering.transform;

import org.joml.Matrix4f;

public class Scale implements LinearTransform{
    private final Matrix4f mat;

    public Scale(float scale) {
        this.mat = new Matrix4f().scale(scale);
    }

    public Scale(float x, float y, float z) {
        this.mat = new Matrix4f().scale(x, y, z);
    }

    @Override
    public Matrix4f matrix() {
        return mat;
    }
}

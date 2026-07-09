package com.vke.core.rendering.transform;

import org.joml.Matrix4f;

public class Translate implements LinearTransform{
    private final Matrix4f mat;

    public Translate(float x, float y, float z) {
        this.mat = new Matrix4f().translate(x, y, z);
    }

    public Translate(float x, float y) {
        this.mat = new Matrix4f().translate(x, y, 0);
    }

    @Override
    public Matrix4f matrix() {
        return mat;
    }
}

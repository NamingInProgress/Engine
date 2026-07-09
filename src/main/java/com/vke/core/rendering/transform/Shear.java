package com.vke.core.rendering.transform;

import org.joml.Matrix4f;

public class Shear implements LinearTransform {
    private final Matrix4f mat;

    public Shear(float x, float y, float z) {
        this.mat = new Matrix4f(
                1, x, x, 0,
                y, 1, y, 0,
                z, z, 1, 0,
                0, 0, 0, 1
        );
    }

    @Override
    public Matrix4f matrix() {
        return mat;
    }
}

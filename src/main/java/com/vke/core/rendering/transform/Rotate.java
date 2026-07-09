package com.vke.core.rendering.transform;

import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class Rotate implements LinearTransform {
    private final Matrix4f mat;

    public Rotate(float z) {
        this.mat = new Matrix4f().rotateZ(z);
    }

    public Rotate(float x, float y, float z) {
        this.mat = new Matrix4f().rotateXYZ(x, y, z);
    }

    public Rotate(Quaternionf quat) {
        this.mat = new Matrix4f().rotate(quat);
    }

    @Override
    public Matrix4f matrix() {
        return mat;
    }
}

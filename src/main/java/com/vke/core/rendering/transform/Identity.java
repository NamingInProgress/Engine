package com.vke.core.rendering.transform;

import org.joml.Matrix4f;

public class Identity implements LinearTransform {
    private static final Matrix4f ID = new Matrix4f();

    @Override
    public Matrix4f matrix() {
        return ID;
    }
}

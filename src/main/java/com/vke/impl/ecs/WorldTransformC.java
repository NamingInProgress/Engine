package com.vke.impl.ecs;

import com.vke.core.ecs.component.Component;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix4f;
import pl.epsi.EcsComponent;

@EcsComponent
public class WorldTransformC implements Component {
    private static final float[] ID_MAT = {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1,
    };

    @Span(16)
    public float[] worldMatrix;

    @Override
    public void initialize(int i) {
        System.arraycopy(ID_MAT, 0, worldMatrix, i, 16);
    }

    @Contract(mutates = "param2")
    public void getMatrix(int i, Matrix4f dest) {
        dest.set(worldMatrix, i * 16);
    }
}

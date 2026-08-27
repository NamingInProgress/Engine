package com.vke.impl.ecs;

import com.vke.core.ecs.component.Component;
import org.joml.Matrix4f;
import pl.epsi.EcsComponent;

@EcsComponent
public class WorldTransformC implements Component {
    public Matrix4f[] matrix;

    @Override
    public void initialize(int i) {
        matrix[i] = new Matrix4f();
    }
}

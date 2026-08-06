package com.vke.impl.ecs;

import com.vke.core.ecs.component.Component;
import pl.epsi.EcsComponent;

@EcsComponent
public class TransformC implements Component {

    public float[] x, y, z;
    public float[] rx, ry, rz;
    public float[] sx, sy, sz;

    public void initialize(int i) {
        sx[i] = sy[i] = sz[i] = 1f;
    }
}

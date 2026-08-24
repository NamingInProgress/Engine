package com.vke.impl.ecs;

import com.vke.core.ecs.component.Component;
import org.joml.Vector3f;
import pl.epsi.EcsComponent;

@EcsComponent
public class TransformC implements Component {

    public float[] x, y, z;
    public float[] rx, ry, rz;
    public float[] sx, sy, sz;

    public void initialize(int i) {
        sx[i] = sy[i] = sz[i] = 1f;
        ry[i] = 90f;
    }

    public Vector3f getNormalizedRotation(int i) {
        return new Vector3f(
                (float) Math.sin(Math.toRadians(rx[i])),
                (float) Math.sin(Math.toRadians(ry[i])),
                (float) Math.sin(Math.toRadians(rz[i]))
        );
    }

}

package com.vke.impl.ecs.camera;

import com.vke.core.color.Color;
import com.vke.core.color.OldColor;
import com.vke.core.ecs.component.Component;
import pl.epsi.EcsComponent;

@EcsComponent
public class PerspectiveCameraC implements Component {

    public float[] fov;
    public float[] nearPlane;
    public float[] farPlane;
    public float[] clearR, clearG, clearB, clearA;

    public void setClearColor(int i, OldColor color) {
        this.clearR[i] = color.x;
        this.clearG[i] = color.y;
        this.clearB[i] = color.z;
        this.clearA[i] = color.w;
    }

    public OldColor getClearColor(int i) {
        return new OldColor(this.clearR[i], this.clearG[i], this.clearB[i], this.clearA[i]);
    }

}

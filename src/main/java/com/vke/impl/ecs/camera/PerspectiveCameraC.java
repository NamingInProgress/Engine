package com.vke.impl.ecs.camera;

import com.vke.core.color.RgbColor;
import com.vke.core.ecs.component.Component;
import pl.epsi.EcsComponent;

@EcsComponent
public class PerspectiveCameraC implements Component {

    public float[] fov;
    public float[] nearPlane;
    public float[] farPlane;
    public float[] clearR, clearG, clearB, clearA;

    public void setClearColor(int i, RgbColor color) {
        this.clearR[i] = color.r();
        this.clearG[i] = color.g();
        this.clearB[i] = color.b();
        this.clearA[i] = color.a();
    }

    public RgbColor getClearColor(int i) {
        return new RgbColor(this.clearR[i], this.clearG[i], this.clearB[i], this.clearA[i]);
    }

}

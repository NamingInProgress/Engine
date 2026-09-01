package com.vke.impl.ecs.light;

import com.vke.core.color.RgbColor;
import com.vke.core.ecs.component.Component;
import pl.epsi.EcsComponent;

@EcsComponent
public class DirectionalLightC implements Component {

    public static final int TYPE = 2;

    public float[] r, g, b;
    public float[] intensity;

    public void initialize(int i, RgbColor col, float intensity) {
        r[i] = col.r();
        g[i] = col.g();
        b[i] = col.b();
        this.intensity[i] = intensity;
    }

}

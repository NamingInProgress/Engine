package com.vke.impl.ecs.light;

import com.vke.core.color.OldColor;
import com.vke.core.ecs.component.Component;
import pl.epsi.EcsComponent;

@EcsComponent
public class PointLightC implements Component {

    public float[] r, g, b;
    public float[] intensity, range;

    public void initialize(int i, OldColor col, float intensity) {
        this.initialize(i, col, intensity, (float) Math.sqrt(intensity / 0.01));
    }

    public void initialize(int i, OldColor col, float intensity, float range) {
        r[i] = col.x;
        g[i] = col.y;
        b[i] = col.z;
        this.intensity[i] = intensity;
        this.range[i] = range;
    }

}

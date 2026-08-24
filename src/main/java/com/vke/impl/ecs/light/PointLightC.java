package com.vke.impl.ecs.light;

import com.vke.core.color.Color;
import com.vke.core.ecs.component.Component;
import com.vke.utils.Utils;
import pl.epsi.EcsComponent;

@EcsComponent
public class PointLightC implements Component {

    public float[] r, g, b;
    public float[] intensity, range;

    public void initialize(int i, Color col, float intensity) {
        this.initialize(i, col, intensity, Utils.rangeFromIntensityLight(intensity, 0.01f));
    }

    public void initialize(int i, Color col, float intensity, float range) {
        r[i] = col.x;
        g[i] = col.y;
        b[i] = col.z;
        this.intensity[i] = intensity;
        this.range[i] = range;
    }

}

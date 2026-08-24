package com.vke.impl.ecs.light;

import com.vke.core.color.Color;
import com.vke.core.ecs.component.Component;
import com.vke.utils.Utils;
import pl.epsi.EcsComponent;

@EcsComponent
public class SpotLightC implements Component {

    public float[] r, g, b;
    public float[] intensity, range;
    public float[] innerConeCos, outerConeCos;

    public void initialize(int i, Color col, float intensity, float innerConeAngle, float outerConeAngle) {
        initialize(i, col, intensity, Utils.rangeFromIntensityLight(intensity), innerConeAngle, outerConeAngle);
    }

    public void initialize(int i, Color col, float intensity, float range, float ica, float oca) {
        r[i] = col.x;
        g[i] = col.y;
        b[i] = col.z;
        this.intensity[i] = intensity;
        this.range[i] = range;
        this.innerConeCos[i] = (float) Math.cos(Math.toRadians(ica / 2));
        this.outerConeCos[i] = (float) Math.cos(Math.toRadians(oca / 2));
    }

}

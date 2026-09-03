package com.vke.impl.ecs.light;

import com.vke.core.color.RgbColor;
import com.vke.core.ecs.component.Component;
import com.vke.utils.Utils;
import pl.epsi.EcsComponent;

@EcsComponent
public class SpotLightC implements Component {

    public static final int TYPE = 1;

    public float[] r, g, b;
    public float[] intensity, range;
    public float[] innerConeCos, outerConeCos;

    public void initialize(int i, RgbColor col, float intensity, float innerConeAngle, float outerConeAngle) {
        initialize(i, col, intensity, autoRange(intensity), innerConeAngle, outerConeAngle);
    }

    public void initialize(int i, RgbColor col, float intensity, float range, float ica, float oca) {
        r[i] = col.r();
        g[i] = col.g();
        b[i] = col.b();
        this.intensity[i] = intensity;
        this.range[i] = range;
        this.innerConeCos[i] = (float) Math.cos(Math.toRadians(ica / 2));
        this.outerConeCos[i] = (float) Math.cos(Math.toRadians(oca / 2));
    }

    @Override
    public void initialize(int i) {
        initialize(i, RgbColor.WHITE, 10, 15, 30);
    }

    public float autoRange(float intensity) {
        return Utils.rangeFromIntensityLight(intensity, 0.001f);
    }
}

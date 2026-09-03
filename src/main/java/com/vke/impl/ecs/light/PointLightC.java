package com.vke.impl.ecs.light;

import com.vke.core.color.RgbColor;
import com.vke.core.ecs.component.Component;
import com.vke.utils.Utils;
import pl.epsi.EcsComponent;

@EcsComponent
public class PointLightC implements Component {

    public static final int TYPE = 0;

    public float[] r, g, b;
    public float[] intensity, range;

    public void initialize(int i, RgbColor col, float intensity) {
        this.initialize(i, col, intensity, autoRange(intensity));
    }

    public void initialize(int i, RgbColor col, float intensity, float range) {
        r[i] = col.r();
        g[i] = col.g();
        b[i] = col.b();
        this.intensity[i] = intensity;
        this.range[i] = range;
    }

    @Override
    public void initialize(int i) {
        initialize(i, RgbColor.WHITE, 10f);
    }

    public float autoRange(float intensity) {
        return Utils.rangeFromIntensityLight(intensity, 0.01f);
    }
}

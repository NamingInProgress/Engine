package com.vke.impl.color;

import com.vke.core.color.Color;
import com.vke.core.color.RgbColor;
import com.vke.core.geom.GeomUtils;
import com.vke.utils.exception.Unreachable;

import java.util.Arrays;

public class HslColor extends Color {
    //H S L A
    //H in radiants
    //S, L, A in 0..1
    private float[] components;

    //https://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion
    public HslColor(RgbColor rgbSource) {
        super(rgbSource);

        float[] rgbComps = rgbSource.getComponents();
        float r = rgbComps[0];
        float g = rgbComps[1];
        float b = rgbComps[2];
        float a = rgbComps[3];

        float vmax = Math.max(r, Math.max(g, b));
        float vmin = Math.min(r, Math.min(g, b));

        float h, s, l = (vmax + vmin) / 2f;

        if (vmax == vmin) {
            this.components = new float[] { 0, 0, l, a };
        } else {
            float d = vmax - vmin;
            s = l > 0.5 ? d / (2 - vmax - vmin) : d / (vmax + vmin);
            if (vmax == r) h = (g - b) / d + (g < b ? 6 : 0);
            else if (vmax == g) h = (b - r) / d + 2;
            else if (vmax == b) h = (r - g) / d + 4;
            else throw new Unreachable();
            h /= 6f;

            this.components = new float[] { h * GeomUtils.PI2F, s, l, a };
        }
    }

    public HslColor(float[] components) {
        super(components);
    }

    @Override
    public float[] getComponents() {
        return components;
    }

    @Override
    public void setComponents(float[] components) {
        this.components = components;
    }

    @Override
    public Color copy() {
        return new HslColor(components.clone());
    }

    //https://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion
    @Override
    public RgbColor toRgb() {
        float h = components[0] / GeomUtils.PI2F;
        float s = components[1];
        float l = components[2];
        float a = components[3];

        float r, g, b;
        if (s == 0) {
            r = g = b = l;
        } else {
            float q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1f/3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1f/3f);
        }

        return new RgbColor(new float[] { r, g, b, a });
    }

    private float hueToRgb(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1f/6f) return p + (q - p) * 6 * t;
        if (t < 1f/2f) return q;
        if (t < 2f/3f) return p + (q - p) * (2f/3f - t) * 6;
        return p;
    }

    @Override
    public String toString() {
        return "HSL: " + Arrays.toString(components);
    }
}

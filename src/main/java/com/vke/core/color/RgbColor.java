package com.vke.core.color;

import java.util.Arrays;

public class RgbColor extends Color {
    private float[] components;

    public RgbColor(RgbColor rgbSource) {
        super(rgbSource);
        this.components = rgbSource.components.clone();
    }

    public RgbColor(float[] components) {
        super(components);
    }

    public RgbColor(float r, float g, float b, float a) {
        this(new float[] { r, g, b, a });
    }

    public RgbColor(float r, float g, float b) {
        this(new float[] { r, g, b, 1 });
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
        return new RgbColor(components.clone());
    }

    @Override
    public RgbColor toRgb() {
        return this;
    }

    public float r() { return components[0]; }
    public float g() { return components[1]; }
    public float b() { return components[2]; }
    public float a() { return components[3]; }

    @Override
    public String toString() {
        return "RGB:" + Arrays.toString(components);
    }
}

package com.vke.core.color;

import com.vke.api.rendering.abstraction.draw.SelfPuttable;
import com.vke.api.rendering.abstraction.renderer.data.RenderingEncoder;
import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;

import java.util.Arrays;

public class RgbColor extends Color implements SelfPuttable {

    public static final RgbColor INVALID = new RgbColor(-1, -1, -1, -1);
    public static final RgbColor BLACK = new RgbColor(0, 0, 0);
    public static final RgbColor WHITE = new RgbColor(1, 1, 1);
    public static final RgbColor VKE = new RgbColor(0.2f, 0.3f, 0.3f);
    public static final RgbColor RED = new RgbColor(1, 0, 0);
    public static final RgbColor GREEN = new RgbColor(0, 1, 0);
    public static final RgbColor BLUE = new RgbColor(0, 0, 1);
    public static final RgbColor YELLOW = new RgbColor(1, 1, 0);
    public static final RgbColor CYAN = new RgbColor(0, 1, 1);
    public static final RgbColor MAGENTA = new RgbColor(1, 0, 1);

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

    @Override
    public void putSelf(RenderingEncoder buf) {
        buf.float4(r(), g(), b(), a());
    }
}

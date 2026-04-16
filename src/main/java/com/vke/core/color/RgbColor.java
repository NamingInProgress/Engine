package com.vke.core.color;

public class RgbColor {
    public static RgbColor BLACK       = new RgbColor(0, 0, 0, 1);
    public static RgbColor WHITE       = new RgbColor(1, 1, 1, 1);

    public static RgbColor RED         = new RgbColor(1, 0, 0, 1);
    public static RgbColor GREEN       = new RgbColor(0, 1, 0, 1);
    public static RgbColor BLUE        = new RgbColor(0, 0, 1, 1);

    public static RgbColor YELLOW      = new RgbColor(1, 1, 0, 1);
    public static RgbColor CYAN        = new RgbColor(0, 1, 1, 1);
    public static RgbColor MAGENTA     = new RgbColor(1, 0, 1, 1);

    public static RgbColor GRAY        = new RgbColor(0.5f, 0.5f, 0.5f, 1);
    public static RgbColor LIGHT_GRAY  = new RgbColor(0.75f, 0.75f, 0.75f, 1);
    public static RgbColor DARK_GRAY   = new RgbColor(0.25f, 0.25f, 0.25f, 1);

    public static RgbColor ORANGE      = new RgbColor(1, 0.5f, 0, 1);
    public static RgbColor PINK        = new RgbColor(1, 0.75f, 0.8f, 1);
    public static RgbColor PURPLE      = new RgbColor(0.5f, 0, 0.5f, 1);

    public static RgbColor BROWN       = new RgbColor(0.6f, 0.3f, 0.1f, 1);
    public static RgbColor LIME        = new RgbColor(0.75f, 1, 0, 1);
    public static RgbColor NAVY        = new RgbColor(0, 0, 0.5f, 1);
    public static RgbColor TEAL        = new RgbColor(0, 0.5f, 0.5f, 1);

    public static RgbColor TRANSPARENT = new RgbColor(0, 0, 0, 0);

    private float r,g,b,a;

    public RgbColor(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public float r() {
        return r;
    }

    public float g() {
        return g;
    }

    public float b() {
        return b;
    }

    public float a() {
        return a;
    }

    public void setR(float r) {
        this.r = r;
    }

    public void setG(float g) {
        this.g = g;
    }

    public void setB(float b) {
        this.b = b;
    }

    public void setA(float a) {
        this.a = a;
    }

    public void set(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
}

package com.vke.core.ui.rendering.core;

public class RoundRectDrawRequest extends DrawRequest {
    private final int x, y, w, h;
    private final int xr, yr;
    private final int stroke;

    public RoundRectDrawRequest(int transform, int clip, int texture, int x, int y, int w, int h, int xr, int yr, int stroke) {
        super(Type.RoundRect, transform, clip, texture);
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.xr = xr;
        this.yr = yr;
        this.stroke = stroke;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public int getXr() {
        return xr;
    }

    public int getYr() {
        return yr;
    }

    public int getStroke() {
        return stroke;
    }
}

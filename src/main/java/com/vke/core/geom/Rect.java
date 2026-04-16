package com.vke.core.geom;

public class Rect {
    public float x, y, w, h;

    public Rect(float x, float y, float w, float h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public Rect(int x, int y, int w, int h) {
        this((float) x, y, w, h);
    }
}

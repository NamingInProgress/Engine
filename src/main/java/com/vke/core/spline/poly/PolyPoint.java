package com.vke.core.spline.poly;

public class PolyPoint {
    public float x, y;
    public boolean isEnd;

    public PolyPoint(float x, float y, boolean isEnd) {
        this.x = x;
        this.y = y;
        this.isEnd = isEnd;
    }

    @Override
    public String toString() {
        return "FlatPoint{" +
                "x=" + x +
                ", y=" + y +
                ", isEnd=" + isEnd +
                '}';
    }
}

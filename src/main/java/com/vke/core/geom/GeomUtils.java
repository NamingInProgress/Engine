package com.vke.core.geom;

public class GeomUtils {
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static float[] lerp2(float x, float y, float x2, float y2, float t) {
        float[] out = new float[2];
        out[0] = lerp(x, x2, t);
        out[1] = lerp(y, y2, t);
        return out;
    }

    public static float distanceToLine(float l1x, float l1y, float l2x, float l2y, float px, float py) {
        float x2d = l2x - l1x;
        float y2d = l2y - l1y;

        return (float)(Math.abs(y2d * px - x2d * py + l2x * l1y - l2y * l1x) / Math.sqrt(y2d * y2d + x2d * x2d));
    }
}

package com.vke.core.geom;

public class GeomUtils {
    public static final double PI_OVER_2 = Math.PI * 0.5f;

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

    public static Vec2 intersectLinesThatGoOnForeverAndWillIntersect(Vec2 p1, Vec2 p2, Vec2 p3, Vec2 p4) {

        double x1 = p1.x, y1 = p1.y;
        double x2 = p2.x, y2 = p2.y;
        double x3 = p3.x, y3 = p3.y;
        double x4 = p4.x, y4 = p4.y;

        double denom =
                (x1 - x2) * (y3 - y4) -
                        (y1 - y2) * (x3 - x4);

        if (Math.abs(denom) < 1e-9) {
            return new Vec2((x2 + x3) * 0.5, (y2 + y3) * 0.5);
        }

        double px =
                ((x1*y2 - y1*x2) * (x3 - x4) -
                        (x1 - x2) * (x3*y4 - y3*x4)) / denom;

        double py =
                ((x1*y2 - y1*x2) * (y3 - y4) -
                        (y1 - y2) * (x3*y4 - y3*x4)) / denom;

        return new Vec2(px, py);
    }
}

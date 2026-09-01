package com.vke.core.geom;

import org.joml.Math;
import org.joml.Vector2f;

public class GeomUtils {
    public static final float PI2F = Math.PI_TIMES_2_f;

    public static Vector2f lerp(Vector2f start, Vector2f end, float t) {
        return new Vector2f(Math.lerp(start.x, end.x, t), Math.lerp(start.y, end.y, t));
    }

    public static float distanceToLine(Vector2f l1, Vector2f l2, Vector2f p) {
        float x2d = l2.x - l1.x;
        float y2d = l2.y - l1.y;

        return Math.abs(y2d * p.x - x2d * p.y + l2.x * l1.y - l2.y * l1.x) / Math.sqrt(y2d * y2d + x2d * x2d);
    }

}

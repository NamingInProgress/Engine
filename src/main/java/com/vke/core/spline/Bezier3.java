package com.vke.core.spline;

import com.vke.core.geom.GeomUtils;

public class Bezier3 {
    private final float p0x, p0y; //start
    private final float p1x, p1y; //control 1
    private final float p2x, p2y; //control 2
    private final float p3x, p3y; //end

    public Bezier3(float p0x, float p0y, float p1x, float p1y, float p2x, float p2y, float p3x, float p3y) {
        this.p0x = p0x;
        this.p0y = p0y;
        this.p1x = p1x;
        this.p1y = p1y;
        this.p2x = p2x;
        this.p2y = p2y;
        this.p3x = p3x;
        this.p3y = p3y;
    }

    public Bezier3[] split(float t) {
        float[] p01 = GeomUtils.lerp2(p0x, p0y, p1x, p1y, t);
        float[] p12 = GeomUtils.lerp2(p1x, p1y, p2x, p2y, t);
        float[] p23 = GeomUtils.lerp2(p2x, p2y, p3x, p3y, t);

        float[] p012 = GeomUtils.lerp2(p01[0], p01[1], p12[0], p12[1], t);
        float[] p123 = GeomUtils.lerp2(p12[0], p12[1], p23[0], p23[1], t);

        float[] p0123 = GeomUtils.lerp2(p012[0], p012[1], p123[0], p123[1], t);

        Bezier3[] out = new Bezier3[2];

        out[0] = new Bezier3(p0x, p0y, p01[0], p01[1], p012[0], p012[1], p0123[0], p0123[1]);
        out[1] = new Bezier3(p0123[0], p0123[1], p123[0], p123[1], p23[0], p23[1], p3x, p3y);

        return out;
    }

    public boolean isBasicallyALine(float tolerance) {
        float d1 = GeomUtils.distanceToLine(p0x, p0y, p3x, p3y, p1x, p1y);
        float d2 = GeomUtils.distanceToLine(p0x, p0y, p3x, p3y, p2x, p2y);
        return d1 <= tolerance && d2 <= tolerance;
    }

    public float[] straightLine() {
        return new float[] { p0x, p0y, p3x, p3y };
    }

    public float[] endPoint() {
        return new float[] { p3x, p3y };
    }
}
package com.vke.core.spline;

import com.vke.core.geom.GeomUtils;

public class Bezier2 {
    private final float p0x, p0y; //start
    private final float p1x, p1y; //control
    private final float p2x, p2y; //end

    public Bezier2(float p0x, float p0y, float p1x, float p1y, float p2x, float p2y) {
        this.p0x = p0x;
        this.p0y = p0y;
        this.p1x = p1x;
        this.p1y = p1y;
        this.p2x = p2x;
        this.p2y = p2y;
    }

    public Bezier2[] split(float t) {
        //derived from de casteljau algorithm
        float[] p01 = GeomUtils.lerp2(p0x, p0y, p1x, p1y, t);
        float[] p12 = GeomUtils.lerp2(p1x, p1y, p2x, p2y, t);
        float[] p012 = GeomUtils.lerp2(p01[0], p01[1], p12[0], p12[1], t);

        Bezier2[] out = new Bezier2[2];
        out[0] = new Bezier2(p0x, p0y, p01[0], p01[1], p012[0], p012[1]);
        out[1] = new Bezier2(p012[0], p012[1], p12[0], p12[1], p2x, p2y);
        return out;
    }

    public boolean isBasicallyALine(float tolerance) {
        float dist = GeomUtils.distanceToLine(p0x, p0y, p2x, p2y, p1x, p1y);
        return dist <= tolerance;
    }

    public float[] straightLine() {
        return new float[] { p0x, p0y, p2x, p2y };
    }

    public float[] endPoint() {
        return new float[] { p2x, p2y };
    }
}

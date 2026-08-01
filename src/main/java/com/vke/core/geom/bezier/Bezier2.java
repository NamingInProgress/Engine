package com.vke.core.geom.bezier;

import com.vke.core.geom.GeomUtils;
import org.joml.Vector2f;

public class Bezier2 {

    private final Vector2f p0; //start
    private final Vector2f p1; //control
    private final Vector2f p2; //end

    public Bezier2(Vector2f p0, Vector2f p1, Vector2f p2) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
    }

    public Bezier2[] split(float t) {
        //derived from de casteljau algorithm
        Vector2f p01 = GeomUtils.lerp(p0, p1, t);
        Vector2f p12 = GeomUtils.lerp(p1, p2, t);
        Vector2f p012 = GeomUtils.lerp(p01, p12, t);

        Bezier2[] out = new Bezier2[2];
        out[0] = new Bezier2(p0, p01, p012);
        out[1] = new Bezier2(p012, p12, p2);
        return out;
    }

    public boolean isBasicallyALine(float tolerance) {
        float dist = GeomUtils.distanceToLine(p0, p2, p1);
        return dist <= tolerance;
    }

    public Vector2f endPoint() {
        return p2;
    }

}

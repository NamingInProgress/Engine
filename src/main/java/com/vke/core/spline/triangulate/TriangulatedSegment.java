package com.vke.core.spline.triangulate;

import com.vke.core.geom.Vec2;
import com.vke.utils.exception.Unreachable;

public class TriangulatedSegment {
    public int[] indices;
    public int[][] vertices;
    public Vec2 directionVector;
    public Vec2 directionVectorNormalized;

    public TriangulatedSegment(int[] indices, int[][] vertices, Vec2 directionVector, Vec2 directionVectorNormalized) {
        this.indices = indices;
        this.vertices = vertices;
        this.directionVector = directionVector;
        this.directionVectorNormalized = directionVectorNormalized;
    }

    public static int findOuterVertex(int innerVertex) {
        return switch (innerVertex) {
            case 0 -> 3;
            case 1 -> 2;
            case 2 -> 1;
            case 3 -> 0;
            default -> throw new Unreachable();
        };
    }

    public Vec2 point(int index) {
        int[] v = vertices[index];
        return new Vec2(v[0], v[1]);
    }

    public void pushToTpl(TriangulatedPolyLine tpl) {
        tpl.begin();
        for (int[] vertex : vertices) {
            tpl.batchVertex(vertex[0], vertex[1]);
        }
        tpl.batchIndices(indices);
    }

    /// {index of this, index of other}
    public int[] findInnerVerticesForJoin(TriangulatedSegment other) {
        Vec2 a = this.directionVectorNormalized;
        Vec2 b = other.directionVectorNormalized;

        double cross = a.cross2D(b);

        boolean turnLeft = cross > 0;

        if (turnLeft) {
            // inner corner is LEFT side
            // this: p2L = 1
            // other: p1L = 0
            return new int[]{1, 0};
        } else {
            // inner corner is RIGHT side
            // this: p2R = 2
            // other: p1R = 3
            return new int[]{2, 3};
        }
    }

    public Vec2 walkDirection(int pointIndex, int sign) {
        Vec2 point = point(pointIndex).copy();
        point.x += directionVector.x * sign;
        point.y += directionVector.y * sign;
        return point;
    }
}

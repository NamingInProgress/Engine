package com.vke.core.spline.poly;

import com.vke.api.rendering.vulkan.buffer.CpuBuffer;
import com.vke.core.geom.GeomUtils;
import com.vke.core.geom.Vec2;
import com.vke.core.spline.SplineStyle;
import com.vke.core.spline.triangulate.TriangulatedPolyLine;
import com.vke.core.spline.triangulate.TriangulatedSegment;

public class PolyLine {
    public final PolyPoint[] points;

    public PolyLine(PolyPoint[] points) {
        this.points = points;
    }

    public TriangulatedPolyLine triangulateContour(SplineStyle.JoinStyle joinStyle, SplineStyle.CapStyle capStyle, int lineWidth) {
        TriangulatedPolyLine tpl = new TriangulatedPolyLine(points.length);

        boolean isCapRn = true;

        float halfWidth = lineWidth * 0.5f;

        TriangulatedSegment prevCache = null;
        for (int i = 0; i < points.length - 1; i++) {
            PolyPoint p1 = points[i];
            PolyPoint p2 = points[i + 1];

            Vec2 direction = new Vec2(p2.x - p1.x, p2.y - p1.y);

            TriangulatedSegment segment;
            if (prevCache != null) {
                segment = prevCache;
                prevCache = null;
            } else {
                segment = buildSegment(p1, p2, halfWidth);
            }

            if (isCapRn) {
                if (capStyle == SplineStyle.CapStyle.Round) {
                    buildRoundCap((int) p1.x, (int) p1.y, direction.direction() + Math.PI, lineWidth, segment.point(0), segment.point(3), tpl);
                }
                isCapRn = false;
            }


            if (p2.isEnd) {
                //place a cap here
                if (capStyle == SplineStyle.CapStyle.Round) {
                    buildRoundCap((int) p2.x, (int) p2.y, direction.direction(), lineWidth, segment.point(2), segment.point(1), tpl);
                }
                isCapRn = true;
                //increment index so that the next segment isnt connected
                i++;
            } else {
                //join with next segment if there is one
                if (i < points.length - 2) {
                    //there is!
                    PolyPoint nextPoint = points[i + 2];
                    TriangulatedSegment nextSegment = buildSegment(p2, nextPoint, halfWidth);
                    //so segments wont be straight here cuz the curve flattening already handles that

                    //first the inner enclosing points
                    int[] innerVertices = segment.findInnerVerticesForJoin(nextSegment);
                    //block so i can reuse variables
                    {
                        int indexForFirst = innerVertices[0];
                        int indexForSecond = innerVertices[1];

                        Vec2 a1 = segment.point(indexForFirst);
                        Vec2 a2 = segment.walkDirection(indexForFirst, -1);

                        Vec2 b1 = nextSegment.point(indexForSecond);
                        Vec2 b2 = nextSegment.walkDirection(indexForSecond, 1);

                        Vec2 join = GeomUtils.intersectLinesThatGoOnForeverAndWillIntersect(a1, a2, b1, b2);

                        segment.vertices[indexForFirst] = nextSegment.vertices[indexForSecond] = join.asInts();
                    }

                    //now the outer vertices
                    if (joinStyle == SplineStyle.JoinStyle.Miter) {
                        int indexForFirst = TriangulatedSegment.findOuterVertex(innerVertices[0]);
                        int indexForSecond = TriangulatedSegment.findOuterVertex(innerVertices[1]);

                        Vec2 a1 = segment.point(indexForFirst);
                        Vec2 a2 = segment.walkDirection(indexForFirst, -1);

                        Vec2 b1 = nextSegment.point(indexForSecond);
                        Vec2 b2 = nextSegment.walkDirection(indexForSecond, 1);

                        Vec2 join = GeomUtils.intersectLinesThatGoOnForeverAndWillIntersect(a1, a2, b1, b2);

                        segment.vertices[indexForFirst] = nextSegment.vertices[indexForSecond] = join.asInts();
                    } else if (joinStyle == SplineStyle.JoinStyle.Round) {

                    }

                    prevCache = nextSegment;
                }
            }

            segment.pushToTpl(tpl);
        }

        return tpl;
    }

    private TriangulatedSegment buildSegment(PolyPoint p1, PolyPoint p2, float halfWidth) {
        Vec2 dirNotNorm = new Vec2(p2.x - p1.x, p2.y - p1.y);
        Vec2 dir = dirNotNorm.normalized();
        Vec2 n = dir.perpendicular();

        double ox = n.x * halfWidth;
        double oy = n.y * halfWidth;

        int x1l = (int) Math.round(p1.x + ox);
        int y1l = (int) Math.round(p1.y + oy);

        int x1r = (int) Math.round(p1.x - ox);
        int y1r = (int) Math.round(p1.y - oy);

        int x2l = (int) Math.round(p2.x + ox);
        int y2l = (int) Math.round(p2.y + oy);

        int x2r = (int) Math.round(p2.x - ox);
        int y2r = (int) Math.round(p2.y - oy);

        int[][] vertices = {
                {x1l, y1l},
                {x2l, y2l},
                {x2r, y2r},
                {x1r, y1r}
        };

        int[] indices = {0, 1, 2, 0, 2, 3};

        return new TriangulatedSegment(indices, vertices, dirNotNorm, dir);
    }

    private void buildRoundCap(int x, int y, double angle, int width, Vec2 startPoint, Vec2 endPoint, TriangulatedPolyLine tpl) {
        int triangleCountEstimate = (int) Math.ceil(CpuBuffer.GROWTH_FAC * Math.sqrt(CpuBuffer.GROWTH_FAC * width));
        double off = angle - GeomUtils.PI_OVER_2;
        double arc = Math.PI;
        buildArc(x, y, off, arc, width / 2, triangleCountEstimate, startPoint, endPoint, tpl);
    }

    private void buildArc(int x, int y, double off, double arc, int radius, int triCnt, Vec2 startPoint, Vec2 endPoint, TriangulatedPolyLine tpl) {
        tpl.begin();
        tpl.batchVertex(x, y);

        for (int i = 0; i <= triCnt; i++) {
            double angle;

            int vx;
            int vy;

            if (i == triCnt) {
                vx = (int) endPoint.x;
                vy = (int) endPoint.y;
            } else if (i == 0) {
                vx = (int) startPoint.x;
                vy = (int) startPoint.y;
            } else {
                double t = (double) i / triCnt;
                angle = off + t * arc;
                vx = (int) (x + Math.cos(angle) * radius);
                vy = (int) (y + Math.sin(angle) * radius);
            }

            tpl.batchVertex(vx, vy);
        }

        for (int i = 1; i <= triCnt; i++) {
            tpl.batchIndices(0, i, i + 1);
        }
    }
}

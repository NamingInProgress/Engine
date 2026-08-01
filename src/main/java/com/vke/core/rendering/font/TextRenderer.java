package com.vke.core.rendering.font;

import com.vke.api.rendering.abstraction.draw.Drawable;
import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.draw.VertexFactory;
import com.vke.api.rendering.abstraction.renderer.data.TexturableEncoder;
import com.vke.core.font.ttf.Glyph;
import com.vke.core.font.ttf.GlyphPoint;
import com.vke.core.geom.bezier.Bezier2;
import com.vke.core.rendering.transform.MatrixStack;
import org.joml.Math;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Arrays;

public class TextRenderer implements Drawable {

    private static final float[] DEFAULT_UV = { 0,0,1,1 };
    private final VertexConsumer<TextRendererVertex> consumer;
    private final MatrixStack matrixStack = new MatrixStack();

    //mutable state
    private float r = 1, g = 1, b = 1, a = 1;
    private float z;

    public TextRenderer(VertexConsumer<TextRendererVertex> consumer) {
        this.consumer = consumer;
    }

    private TextRendererVertex v(Vector2f a) {
        return new TextRendererVertex(a.x, a.y, z, r, g, b, this.a, 0, 0, matrixStack.currentMatrixIndex());
    }

    //@ChatGPT("Fix this, no mistakes plz")
    public void glyph(Glyph g) {
        int contourStartIndex = 0;
        int[] endPointsOfContours = g.endPointsOfContours;
        for (int i = 0; i < endPointsOfContours.length; i++) {
            int contourEndIndex = endPointsOfContours[i];
            int numPoints = contourEndIndex - contourStartIndex + 1;

            for (int j = contourStartIndex; j < contourStartIndex + numPoints; j++) {
                int next = (j == contourEndIndex) ? contourStartIndex : j + 1;
                GlyphPoint point = g.points[j];
                GlyphPoint nextPoint = g.points[next];

                if (point.onCurve && nextPoint.onCurve) {
                    //line(point.vec, nextPoint.vec);
                } else {
                    int next2;

                    if (next == contourEndIndex)
                        next2 = contourStartIndex;
                    else
                        next2 = next + 1;
                    //quadBezier(point.vec, nextPoint.vec, g.points[next2].vec);
                    tri(point.vec, nextPoint.vec, g.points[next2].vec);
                    j += 1;
                }
            }

            contourStartIndex = contourEndIndex + 1;
        }
    }

    public void line(Vector2f a, Vector2f b) {
        consumer.begin();
        consumer.vertices(v(a), v(b));
        consumer.indices(0, 1);
    }

    public void tri(Vector2f a, Vector2f b, Vector2f c) {
        consumer.begin();
        consumer.vertices(vu(a, 0, 0), vu(b, 0.5f, 0), vu(c, 1, 1));
        consumer.indices(0, 1, 2);
    }

    public TextRendererVertex vu(Vector2f vec, float u, float v) {
        return new TextRendererVertex(vec.x, vec.y, z, r, g, b, a, u, v, matrixStack.currentMatrixIndex());
    }

    public void quadBezier(Vector2f a, Vector2f control, Vector2f b) {
        ArrayList<Vector2f> points = new ArrayList<>();
        points.add(new Vector2f(a));

        quadRecursive(new Bezier2(a, control, b), points, 0.5f);

        for (int i = 0; i < points.size(); i++) {
            if (i == points.size() - 1) continue;
            line(points.get(i), points.get(i + 1));
        }
    }

    private void quadRecursive(Bezier2 bezier, ArrayList<Vector2f> out, float tolerance) {
        if (bezier.isBasicallyALine(tolerance)) {
            Vector2f endPoint = bezier.endPoint();
            out.add(new Vector2f(endPoint));
        } else {
            Bezier2[] split = bezier.split(0.5f);
            quadRecursive(split[0], out, tolerance);
            quadRecursive(split[1], out, tolerance);
        }
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    @Override
    public void draw() {
        consumer.draw();
        matrixStack.reset();
    }

    @Override
    public void drawInstanced(int instanceCount) {
        consumer.drawInstanced(instanceCount);
        matrixStack.reset();
    }

    public static class TextRendererVertex implements Vertex {
        public static final TextRendererVertex TEMPLATE = new TextRendererVertex(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        private final float x, y, z, r, g, b, a, u, v;
        private final int matId;

        public TextRendererVertex(float x, float y, float z, float r, float g, float b, float a, float u, float v, int matId) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.u = u;
            this.v = v;
            this.matId = matId;
        }

        @Override
        public int getByteStride() {
            return 10 * 4;
        }

        @Override
        public void putSelf(TexturableEncoder buf) {
            buf.float3(x, y, z);
            buf.float4(r, g, b, a);
            buf.float2(u, v);
            buf.int1(matId);
        }
    }

}

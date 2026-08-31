package com.vke.core.rendering.font;

import com.vke.api.font.Font;
import com.vke.api.font.FontCursor;
import com.vke.api.rendering.abstraction.draw.Drawable;
import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.draw.VertexConsumerProvider;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.core.color.OldColor;
import com.vke.core.font.ttf.Glyph;
import com.vke.core.font.ttf.GlyphPoint;
import com.vke.core.rendering.transform.MatrixStack;
import org.joml.Vector2f;
import pl.epsi.MakeVertex;
import pl.epsi.Type;

import java.util.List;

public class TextRenderer implements Drawable {

    private final VertexConsumer<BezierVertex> bezier;
    private final VertexConsumer<RegularVertex> regular;
    private final VertexConsumer<QuadVertex> quad;
    private final MatrixStack matrixStack = new MatrixStack();

    private final TextRendererPass1DriverBezier pass1Bezier;
    private final TextRendererPass1DriverRegular pass1;
    private final TextRendererPass2Driver pass2;

    private final Font font;

    //mutable state
    private float baseX, baseY;
    private float r = 1, g = 1, b = 1, a = 1;
    private float z;
    private float fontSize; // This is **not** the font scale calculated by size / font.unitsPerEm(). This is the size.
    private float fontScale;

    public TextRenderer(RenderSystem sys, Font font, VertexConsumerProvider provider) {
        this.font = font;

        this.bezier = provider.get(BezierVertex.TEMPLATE);
        this.regular = provider.get(RegularVertex.TEMPLATE);
        this.quad = provider.get(QuadVertex.TEMPLATE);

        this.pass1 = new TextRendererPass1DriverRegular(sys);
        this.pass1Bezier = new TextRendererPass1DriverBezier(sys);
        this.pass2 = new TextRendererPass2Driver(sys);
    }

    public void accept(FontCursor cursor, boolean reset) {
        List<FontCursor.GlyphInfo> data = cursor.read();
        matrixStack.push();
        for (FontCursor.GlyphInfo datum : data) {
            if (fontSize != datum.fontSize()) {
                this.fontSize = datum.fontSize();
                this.fontScale = fontSize / (float) font.unitsPerEm();
                matrixStack.pop();
                matrixStack.push();
                matrixStack.scale(fontScale);
            }

            this.baseX = datum.x() / fontScale;
            this.baseY = datum.y() / fontScale;

            glyph(datum.g());
        }
        matrixStack.pop();
        fontSize = -1;

        if (reset) cursor.reset();
    }

    //@ChatGPT("Fix this, no mistakes plz")
    private void glyph(Glyph g) {
        Vector2f center = new Vector2f((g.xMin + g.xMax) / 2f, (g.yMin + g.yMax) / 2f);
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
                    regularTri(center, point.vec, nextPoint.vec);
                } else {
                    int next2;

                    if (next == contourEndIndex)
                        next2 = contourStartIndex;
                    else
                        next2 = next + 1;

                    regularTri(center, point.vec, g.points[next2].vec);
                    bezierTri(point.vec, nextPoint.vec, g.points[next2].vec);
                    j += 1;
                }
            }

            contourStartIndex = contourEndIndex + 1;
        }

        boundingBox(g);
    }

    public void boundingBox(Glyph g) {
        quad.begin();
        quad.vertices(quad_vert(g.xMin, g.yMin), quad_vert(g.xMax, g.yMin), quad_vert(g.xMax, g.yMax), quad_vert(g.xMin, g.yMax));
        quad.indices(0, 1, 2, 2, 3, 0);
    }

    public void regularTri(Vector2f a, Vector2f b, Vector2f c) {
        regular.begin();
        regular.vertices(regular_vert(a), regular_vert(b), regular_vert(c));
        regular.indices(0, 1, 2);
    }

    public void bezierTri(Vector2f a, Vector2f b, Vector2f c) {
        bezier.begin();
        bezier.vertices(bezier_vert(a, 0, 0), bezier_vert(b, 0.5f, 0), bezier_vert(c, 1, 1));
        bezier.indices(0, 1, 2);
    }

    public BezierVertex bezier_vert(Vector2f vec, float u, float v) {
        return new BezierVertex(baseX + vec.x, baseY + vec.y, z, u, v, matrixStack.currentMatrixIndex());
    }

    public RegularVertex regular_vert(Vector2f vec) {
        return new RegularVertex(baseX + vec.x, baseY + vec.y, z, matrixStack.currentMatrixIndex());
    }

    public QuadVertex quad_vert(float x, float y) {
        return new QuadVertex(baseX + x, baseY + y, z, r, g, b, a, matrixStack.currentMatrixIndex());
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public void color(OldColor color) {
        this.r = color.r();
        this.g = color.g();
        this.b = color.b();
        this.a = color.a();
    }

    public void color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void z(float z) {
        this.z = z;
    }

    public void xOffset(float xOffset) {
        this.baseX = xOffset;
    }

    public void yOffset(float yOffset) {
        this.baseY = yOffset;
    }

    public void render(CommandBuffer cmd, CommandBuffer.AttachmentInfo color, Texture stencil, Texture depth) {
        cmd.beginRendering(new CommandBuffer.RenderingInfo(
                (CommandBuffer.AttachmentInfo) null,
                CommandBuffer.AttachmentInfo.depth(depth),
                CommandBuffer.AttachmentInfo.stencil(stencil)
        ));

        pass1.setMatrixStack(matrixStack);
        pass1.use();

        regular.draw();

        pass1Bezier.setMatrixStack(matrixStack);
        pass1Bezier.use();

        bezier.draw();

        cmd.endRendering();

        cmd.beginRendering(new CommandBuffer.RenderingInfo(
                color,
                new CommandBuffer.AttachmentInfo(depth, LoadOp.LOAD, StoreOp.STORE),
                new CommandBuffer.AttachmentInfo(stencil, LoadOp.LOAD, StoreOp.STORE)
        ));

        pass2.setMatrixStack(matrixStack);
        pass2.use();

        quad.draw();

        cmd.endRendering();

        matrixStack.reset();
    }

    @Override
    public void draw() {
        throw new RuntimeException("Text Renderer does not support regular draw! Uses multiple pipelines! Use the render method instead.");
    }

    @Override
    public void drawInstanced(int instanceCount) {
        throw new RuntimeException("Text Renderer does not support regular draw! Uses multiple pipelines! Use the render method instead.");
    }

    @MakeVertex
    public static class BezierVertex implements Vertex {
        public static final BezierVertex TEMPLATE = null;

        @Type.Float3
        private final float x, y, z;
        @Type.Float2
        private final float u, v;
        @Type.Int1
        private final int matId;

        public BezierVertex(float x, float y, float z, float u, float v, int matId) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
            this.matId = matId;
        }

    }

    @MakeVertex
    public static class RegularVertex implements Vertex {
        public static final RegularVertex TEMPLATE = null;

        @Type.Float3
        private final float x, y, z;
        @Type.Int1
        private final int matId;

        public RegularVertex(float x, float y, float z, int matId) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.matId = matId;
        }
    }

    @MakeVertex
    public static class QuadVertex implements Vertex {
        public static final QuadVertex TEMPLATE = null;

        @Type.Float3
        private final float x, y, z;
        @Type.Float4
        private final float r, g, b, a;
        @Type.Int1
        private final int matId;

        public QuadVertex(float x, float y, float z, float r, float g, float b, float a, int matId) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.matId = matId;
        }

    }

}

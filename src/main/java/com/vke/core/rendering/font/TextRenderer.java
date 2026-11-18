package com.vke.core.rendering.font;

import com.vke.api.assets.AssetHandle;
import com.vke.api.font.Font;
import com.vke.api.font.FontFace;
import com.vke.api.rendering.abstraction.draw.Drawable;
import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.BufferResource;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.buf.FieldArrayResource;
import com.vke.core.color.RgbColor;
import com.vke.core.font.ttf.Glyph;
import com.vke.core.font.ttf.GlyphPoint;
import com.vke.core.rendering.graph2.GraphTexture;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.rendering.transform.MatrixStack;
import com.vke.utils.exception.Unreachable;
import org.joml.Vector2f;
import pl.epsi.MakeVertex;
import pl.epsi.Type;

import java.io.IOException;
import java.util.List;

public class TextRenderer implements Drawable {

    private final RenderSystem sys;

    private final VertexConsumer<BezierVertex> bezier;
    private final VertexConsumer<RegularVertex> regular;
    private final VertexConsumer<QuadVertex> quad;
    private final MatrixStack matrixStack = new MatrixStack();

    private final TextRendererPass1DriverBezier pass1Bezier;
    private final TextRendererPass1DriverRegular pass1;
    private final TextRendererPass2Driver pass2;

    //mutable state
    private Font font;
    private float baseX, baseY;
    private float r = 1, g = 1, b = 1, a = 1;
    private float z;
    private float actualScale;

    private boolean changedScale = true;

    private int overrideMatrixIndex = -1;

    public TextRenderer(RenderSystem sys) {
        this.sys = sys;

        var provider = sys.vcp();
        this.bezier = provider.get(BezierVertex.TEMPLATE);
        this.regular = provider.get(RegularVertex.TEMPLATE);
        this.quad = provider.get(QuadVertex.TEMPLATE);

        this.pass1 = new TextRendererPass1DriverRegular(sys);
        this.pass1Bezier = new TextRendererPass1DriverBezier(sys);
        this.pass2 = new TextRendererPass2Driver(sys);
    }

    public void overrideMatrixIndex(int i) {
        this.overrideMatrixIndex = i;
    }

    public void resetOverrideMatrixIndex() { this.overrideMatrixIndex = -1; }

    public void drawText(AssetHandle<FontFace> fontHandle, int fontSize, String fontSelector, String text) {
        setup(unwrapFontFace(fontHandle), fontSelector, fontSize);
        write(text);
    }

    public void drawText(FontFace font, int fontSize, String fontSelector, String text) {
        setup(font, fontSelector, fontSize);
        write(text);
    }

    public void drawText(FontFace font, int fontSize, int fontSelector, String text) {
        setup(font, fontSelector, fontSize);
        write(text);
    }

    private void setup(FontFace ff, int selector, int fontSize) {
        this.font = ff.get(selector);
//        this.baseX = 0; // We probably don't want to reset it because then all drawn text would overlap, dump the responsibility to the user
//        this.baseY = 0;
        float tempScale = (float) fontSize / font.unitsPerEm();
        if (tempScale != actualScale) changedScale = true;
        this.actualScale = tempScale;
    }

    private void setup(FontFace ff, String selector, int fontSize) {
        setup(ff, ff.toFlags(selector), fontSize);
    }

    private FontFace unwrapFontFace(AssetHandle<FontFace> fontHandle) {
        try {
            return fontHandle.acquire(sys);
        } catch (IOException e) {
            sys.throwException(new RuntimeException("Failed to obtain font face from asset handle: " + fontHandle), "TextRenderer#unwrapFontFace");
            throw new Unreachable();
        }
    }

    private void write(String text) {
        if (changedScale && overrideMatrixIndex == -1) {
            matrixStack.push();
            matrixStack.scale(actualScale);
        }
        int lastGlyphIndex = -1;
        for (int i = 0; i < text.length();) {
            int codePoint = text.codePointAt(i);
            Glyph g = font.getGlyph(codePoint);

            if (lastGlyphIndex != -1) {
                float kernOffset = font.kern(lastGlyphIndex, g.glyphIndex);
                this.baseX += kernOffset;
            }

            this.baseX += (g.leftSideBearing);

            lastGlyphIndex = g.glyphIndex;

            glyph(g);

            this.baseX += g.advanceWidth;

            i += Character.charCount(codePoint);
        }

        if (changedScale && overrideMatrixIndex == -1) {
            matrixStack.pop();
            changedScale = false;
        }
    }

//    @ChatGPT("Fix this, no mistakes plz")
    private void glyph(Glyph g) {
        Vector2f center = new Vector2f((g.xMin + g.xMax) / 2f, (g.yMin + g.yMax) / 2f);
        int contourStartIndex = 0;
        int[] endPointsOfContours = g.endPointsOfContours;
        for (int contourEndIndex : endPointsOfContours) {
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

    private void boundingBox(Glyph g) {
        quad.begin();
        quad.vertices(quad_vert(g.xMin, g.yMin), quad_vert(g.xMax, g.yMin), quad_vert(g.xMax, g.yMax), quad_vert(g.xMin, g.yMax));
        quad.indices(0, 1, 2, 2, 3, 0);
    }

    private void regularTri(Vector2f a, Vector2f b, Vector2f c) {
        regular.begin();
        regular.vertices(regular_vert(a), regular_vert(b), regular_vert(c));
        regular.indices(0, 1, 2);
    }

    private void bezierTri(Vector2f a, Vector2f b, Vector2f c) {
        bezier.begin();
        bezier.vertices(bezier_vert(a, 0, 0), bezier_vert(b, 0.5f, 0), bezier_vert(c, 1, 1));
        bezier.indices(0, 1, 2);
    }

    private int idx() {
        return overrideMatrixIndex == -1 ? matrixStack.currentMatrixIndex() : overrideMatrixIndex;
    }

    private BezierVertex bezier_vert(Vector2f vec, float u, float v) {
        return new BezierVertex(baseX + vec.x, baseY + vec.y, z, u, v, matrixStack.currentMatrixIndex());
    }

    private RegularVertex regular_vert(Vector2f vec) {
        return new RegularVertex(baseX + vec.x, baseY + vec.y, z, matrixStack.currentMatrixIndex());
    }

    private QuadVertex quad_vert(float x, float y) {
        return new QuadVertex(baseX + x, baseY + y, z, r, g, b, a, matrixStack.currentMatrixIndex());
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public void color(RgbColor color) {
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

    public FieldArrayResource getMatrixBuffer() { return this.pass1.buf; }

    public void render(CommandBuffer cmd, RenderPass pass, GraphTexture color, GraphTexture stencil, GraphTexture depth) {
        cmd.beginRendering(new CommandBuffer.RenderingInfo(
                (CommandBuffer.AttachmentInfo) null,
                new CommandBuffer.AttachmentInfo(depth.extract(), LoadOp.LOAD, StoreOp.STORE),
                CommandBuffer.AttachmentInfo.stencil(stencil.extract())
        ));
//        pass.beginRendering(cmd, List.of(), depth, stencil, List.of(), RgbColor.WHITE, RgbColor.BLACK);

        if (overrideMatrixIndex == -1) {
            pass1.setMatrixStack(matrixStack);
        }
        pass1.use();

        regular.draw();

        if (overrideMatrixIndex == -1) {
            pass1Bezier.setMatrixStack(matrixStack);
        }
        pass1Bezier.use();

        bezier.draw();

        cmd.endRendering();

//        pass.beginRendering(cmd, List.of(color), null, stencil, List.of(RgbColor.VKE), RgbColor.WHITE, RgbColor.BLACK);
        cmd.beginRendering(new CommandBuffer.RenderingInfo(
                new CommandBuffer.AttachmentInfo(color.extract(), LoadOp.LOAD, StoreOp.STORE),
                new CommandBuffer.AttachmentInfo(depth.extract(), LoadOp.LOAD, StoreOp.STORE),
                new CommandBuffer.AttachmentInfo(stencil.extract(), LoadOp.LOAD, StoreOp.STORE)
        ));

        if (overrideMatrixIndex == -1) {
            pass2.setMatrixStack(matrixStack);
        }
        pass2.use();

        quad.draw();

        cmd.endRendering();

        if (overrideMatrixIndex == -1)
            matrixStack.reset();

        changedScale = true;
        baseX = 0;
        baseY = 0;
        resetOverrideMatrixIndex();
    }

    @Override
    public void draw() {
        throw new RuntimeException("Text Renderer does not support regular draw! Uses multiple pipelines! Use the render method instead.");
    }

    @Override
    public void drawInstanced(int instanceCount) {
        throw new RuntimeException("Text Renderer does not support regular draw! Uses multiple pipelines! Use the render method instead.");
    }

    @Override
    public void drawInstanced(int instanceCount, int firstInstance) {
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

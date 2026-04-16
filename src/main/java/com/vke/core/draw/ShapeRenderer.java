package com.vke.core.draw;

import com.vke.api.draw.*;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.core.rendering.draw.DrawContext;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public class ShapeRenderer<T extends Vertex> implements Drawable {

    private static final float[] DEFAULT_UV = { 0,0,1,1 };
    private final VertexConsumer<T> consumer;
    private final VertexFactory<T> factory;

    //mutable state
    private float r,g,b,a;
    private QuadTexture texture;
    private Texture t;
    private float z;

    public ShapeRenderer(VertexConsumer<T> consumer, VertexFactory<T> factory) {
        this.consumer = consumer;
        this.factory = factory;
    }

    public void color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public void texture(@Nullable QuadTexture texture) {
        this.texture = texture;
        if (texture != null) {
            this.t = texture.texture();
        } else {
            this.t = null;
        }
    }
    
    public void z(float z) {
        this.z = z;
    }

    private T v(float x, float y, float z, float u, float v) {
        return factory.apply(x, y, z, r, g, b, a, u, v, t);
    }

    /**
     * Interpolates the given xy inside the bounding box. there is the positional bounding box with bx by bw bh and the uv bounding box with bu bv btw bth
     */
    private T vIpltUV(float x, float y, float z, float bx, float by, float bw, float bh, float bu, float bv, float btw, float bth) {
        float nx = (x - bx) / bw;
        float ny = (y - by) / bh;

        float u = bu + nx * btw;
        float v = bv + ny * bth;

        return factory.apply(x, y, z, r, g, b, a, u, v, t);
    }
    
    private float[] uvwh() {
        if (texture == null) {
            return DEFAULT_UV;
        } else {
            return texture.uvFor();
        }
    }

    public void rect(int x, int y, int w, int h) {
        float[] uvwh = uvwh();
        float u = uvwh[0]; float v = uvwh[1]; float tw = uvwh[2]; float th = uvwh[3];
        consumer.begin();
        consumer.vertices(
                v(x, y, z, u, v),
                v(x, y + h, z, u, v + th),
                v(x + w, y + h, z, u + tw, v + th),
                v(x + w, y, z, u + tw, v)
        );
        consumer.indices(0, 1, 2, 2, 3, 0);
        texture(null);
    }

    public void triangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        int minX = Math.min(x1, Math.min(x2, x3));
        int minY = Math.min(y1, Math.min(y2, y3));
        int maxX = Math.max(x1, Math.max(x2, x3));
        int maxY = Math.max(y1, Math.max(y2, y3));
        int bw = maxX - minX;
        int bh = maxY - minY;

        float[] uvwh = uvwh();
        float u = uvwh[0]; float v = uvwh[1]; float tw = uvwh[2]; float th = uvwh[3];
        consumer.begin();
        consumer.vertices(t,
                vIpltUV(x1, y1, z, minX, minY, bw, bh, u, v, tw, th),
                vIpltUV(x2, y2, z, minX, minY, bw, bh, u, v, tw, th),
                vIpltUV(x3, y3, z, minX, minY, bw, bh, u, v, tw, th)
        );
        consumer.indices(0, 1, 2);
        texture(null);
    }

    /// values in degrees
    public void ovalArc(int x, int y, int rx, int ry, double off, double arc, int triCnt) {
        double offRad = Math.toRadians(off);
        double arcRad = Math.toRadians(arc);

        int bx = x - rx;
        int by = y - ry;
        int bw = rx + rx;
        int bh = ry + ry;

        float[] uvwh = uvwh();
        float u = uvwh[0]; float v = uvwh[1]; float tw = uvwh[2]; float th = uvwh[3];

        consumer.begin();
        consumer.vertices(t, vIpltUV(x, y, z, bx, by, bw, bh, u, v, tw, th));
        for (int i = 0; i < triCnt; i++) {
            double t = (double) i / triCnt;
            double angle = offRad + t * arcRad;

            float vx = (float) (x + Math.cos(angle) * rx);
            float vy = (float) (y + Math.sin(angle) * ry);

            consumer.vertices(this.t, vIpltUV(vx, vy, z, bx, by, bw, bh, u, v, tw, th));
        }

        for (int i = 1; i <= triCnt; i++) {
            consumer.indices(0, i, i + 1);
        }
        texture(null);
    }

    public void oval(int x, int y, int rx, int ry, int triCnt) {
        ovalArc(x, y, rx, ry, 0, 360, triCnt);
    }

    public void arc(int x, int y, int r, double off, double arc, int triCnt) {
        ovalArc(x, y, r, r, off, arc, triCnt);
    }

    public void circle(int x, int y, int r, int triCnt) {
        oval(x, y, r, r, triCnt);
    }

    @Override
    public void draw(DrawContext ctx) {
        consumer.draw(ctx);
    }
}

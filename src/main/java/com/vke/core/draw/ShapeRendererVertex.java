package com.vke.core.draw;

import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.abstraction.draw.VertexFactory;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.data.VertexEncoder;

public class ShapeRendererVertex implements Vertex {

    public static final ShapeRendererVertex TEMPLATE = new ShapeRendererVertex(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, null);
    public static final VertexFactory FACTORY = ShapeRendererVertex::new;

    private float x, y, z;
    private float r, g, b, a;
    private float u, v;
    private int matId;
    private final Texture texture;

    public ShapeRendererVertex(float x, float y, float z, float r, float g, float b, float a, float u, float v, int matId, Texture texture) {
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
        this.texture = texture;
    }

    @Override
    public int getByteStride() {
        return Float.BYTES * 9 + Integer.BYTES * 2;
    }

    @Override
    public void putSelf(VertexEncoder buf) {
        buf.float3(x, y, z);
        buf.float4(r, g, b, a);
        buf.float2(u, v);
        buf.sampler2D(texture);
        buf.int1(matId);
    }

}

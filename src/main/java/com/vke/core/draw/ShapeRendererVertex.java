package com.vke.core.draw;

import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.abstraction.draw.VertexFactory;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import pl.epsi.MakeVertex;
import pl.epsi.Type;

@MakeVertex
public class ShapeRendererVertex implements Vertex {

    public static final ShapeRendererVertex TEMPLATE = null;
    public static final VertexFactory FACTORY = ShapeRendererVertex::new;

    @Type.Float3
    private float x, y, z;
    @Type.Float4
    private float r, g, b, a;
    @Type.Float2
    private float u, v;
    @Type.Int1
    private int matId;
    @Type.Sampler2D
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

}

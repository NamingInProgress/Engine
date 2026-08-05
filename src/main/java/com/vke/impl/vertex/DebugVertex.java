package com.vke.impl.vertex;

import com.vke.api.rendering.abstraction.draw.Vertex;
import pl.epsi.MakeVertex;
import pl.epsi.Type;

@MakeVertex
public class DebugVertex implements Vertex {

    public static final DebugVertex TEMPLATE = null;

    @Type.Float3
    private final float x, y, z;
    @Type.Float4
    private final float r, g, b, a;

    public DebugVertex(float x, float y, float z, float r, float g, float b, float a) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
}

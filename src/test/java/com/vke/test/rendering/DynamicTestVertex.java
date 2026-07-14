package com.vke.test.rendering;

import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.abstraction.renderer.data.VertexEncoder;

public class DynamicTestVertex implements Vertex {

    private final float x, y, z;
    private final float r, g, b, a;

    public DynamicTestVertex(float x, float y, float z, float r, float g, float b, float a) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public int getByteStride() {
        return 4 * 7;
    }

    @Override
    public void putSelf(VertexEncoder buf) {
        buf.float3(x, y, z);
        buf.float4(r, g, b, a);
    }
}

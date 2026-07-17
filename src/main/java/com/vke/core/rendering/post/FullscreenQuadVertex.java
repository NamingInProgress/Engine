package com.vke.core.rendering.post;

import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.abstraction.renderer.data.VertexEncoder;

public class FullscreenQuadVertex implements Vertex {

    public static final FullscreenQuadVertex TEMPLATE = new FullscreenQuadVertex(0, 0, 0, 0);

    private float x, y, u, v;

    public FullscreenQuadVertex(float x, float y, float u, float v) {
        this.x = x;
        this.y = y;
        this.u = u;
        this.v = v;
    }

    @Override
    public int getByteStride() {
        return 4 * Float.BYTES;
    }

    @Override
    public void putSelf(VertexEncoder buf) {
        buf.float2(x, y);
        buf.float2(u, v);
    }
}

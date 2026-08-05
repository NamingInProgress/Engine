package com.vke.core.rendering.post;

import com.vke.api.rendering.abstraction.draw.Vertex;
import pl.epsi.MakeVertex;
import pl.epsi.Type;

@MakeVertex
public class FullscreenQuadVertex implements Vertex {

    public static final FullscreenQuadVertex TEMPLATE = null;

    @Type.Float2
    private float x, y, u, v;

    public FullscreenQuadVertex(float x, float y, float u, float v) {
        this.x = x;
        this.y = y;
        this.u = u;
        this.v = v;
    }

//    @Override
//    public int getByteStride() {
//        return 4 * Float.BYTES;
//    }
//
//    @Override
//    public void putSelf(TexturableEncoder buf) {
//        buf.float2(x, y);
//        buf.float2(u, v);
//    }
}

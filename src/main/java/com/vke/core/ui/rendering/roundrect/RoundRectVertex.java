package com.vke.core.ui.rendering.roundrect;

import com.vke.api.draw.Vertex;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.vulkan.buffer.VertexByteSink;
import org.jetbrains.annotations.Nullable;

public class RoundRectVertex extends Vertex {
    // Basic Vertex Data
    private float x, y, z;
    private float r, g, b, a;
    private float u, v;
    private final Texture texture;

    // SDF Specific Data
    private float centerX, centerY;
    private float halfWidth, halfHeight;
    private float radiusX, radiusY;
    private float strokeWidth;

    public static final RoundRectVertex EMPTY = new RoundRectVertex(0,0,0,0,0,0,0,0,0,null,0,0,0,0,0,0,0);

    public RoundRectVertex(
            float x, float y, float z,
            float r, float g, float b, float a,
            float u, float v,
            Texture texture,
            float centerX, float centerY,
            float halfWidth, float halfHeight,
            float radiusX, float radiusY,
            float strokeWidth
    ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.u = u;
        this.v = v;
        this.texture = texture;
        this.centerX = centerX;
        this.centerY = centerY;
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.strokeWidth = strokeWidth;
    }

    @Override
    public int getByteStride() {
        // pos(3) + color(4) + uv(2) + texId(1) + center(2) + halfSize(2) + radii(2)
        // (3+4+2+1+2+2+2) * 4 bytes = 16 floats/ints * 4 = 64 bytes
        return Float.BYTES * 15 + Integer.BYTES;
    }

    @Override
    public @Nullable Texture usesTexture() {
        return this.texture;
    }

    @Override
    public void putSelf(VertexByteSink buf) {
        // Location 0, 1, 2, 3
        buf.float3(x, y, z);
        buf.float4(r, g, b, a);
        buf.float2(u, v);
        buf.int1(texId());

        // Location 4: vec2 inCenter
        buf.float2(centerX, centerY);

        // Location 5: vec2 inHalfSize
        buf.float2(halfWidth, halfHeight);

        // Location 6: vec2 inRadii
        buf.float2(radiusX, radiusY);
        buf.float1(strokeWidth);
    }
}

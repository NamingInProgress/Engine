package com.vke.core.ui.rendering.core;

import com.vke.api.draw.Vertex;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.rendering.vulkan.buffer.VertexByteSink;
import org.jetbrains.annotations.Nullable;

public class UiGeneralVertex extends Vertex {
    public static final UiGeneralVertex EMPTY = new UiGeneralVertex(
            0, 0, 0, 0, 0, 0, 0, 0, 0, null, -1, -1
    );

    private float x, y, z;
    private float r, g, b, a;
    private float u, v;
    private final Texture texture;
    private final int transform, clip;

    public UiGeneralVertex(float x, float y, float z, float r, float g, float b, float a, float u, float v, Texture texture, int transform, int clip) {
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
        this.transform = transform;
        this.clip = clip;
    }

    @Override
    public int getByteStride() {
        return Float.BYTES * 9 + Integer.BYTES * 3;
    }

    @Override
    public @Nullable Texture usesTexture() {
        return this.texture;
    }

    @Override
    public void putSelf(VertexByteSink buf) {
        buf.float3(x, y, z);
        buf.float4(r, g, b, a);
        buf.float2(u, v);
        buf.int1(texId());
        buf.int1(transform);
        buf.int1(clip);
    }
}
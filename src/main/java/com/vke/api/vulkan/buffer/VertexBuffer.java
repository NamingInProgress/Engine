package com.vke.api.vulkan.buffer;

import com.vke.utils.Disposable;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public abstract class VertexBuffer implements Disposable {
    private static final double GROWTH_FAC = 1.61803398874989490252573887119069695472717285156250;

    protected ByteBuffer data;

    public int vertexCount;
    protected int vertexCapacity;

    public VertexBuffer(int baseVertexCount) {
        int allocSize = baseVertexCount * getByteStride();
        data = MemoryUtil.memCalloc(allocSize);
        vertexCount = 0;
        vertexCapacity = baseVertexCount;
    }

    protected void ensureSpaceForVertices(int n) {
        int newCount = vertexCount + n;
        if (newCount > vertexCapacity) {
            while (newCount > vertexCapacity) {
                vertexCapacity = (int) (((double) vertexCapacity) * GROWTH_FAC);
            }
            data = MemoryUtil.memRealloc(data, vertexCapacity * getByteStride());
        }
    }

    public ByteBuffer getData() {
        return data;
    }

    public abstract int getByteStride();

    public static int t_float() {
        return 4;
    }

    public static int t_vec2() {
        return t_float() * 2;
    }

    public static int t_vec3() {
        return t_float() * 3;
    }

    public static int t_vec4() {
        return t_float() * 4;
    }

    @Override
    public void free() {
        MemoryUtil.memFree(data);
    }
}

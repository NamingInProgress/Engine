package com.vke.api.vulkan.buffer;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public abstract class VertexBuffer extends CpuBuffer {
    protected ByteBuffer data;

    public VertexBuffer(int baseVertexCount) {
        super(baseVertexCount);
    }

    @Override
    protected void alloc(int size) {
        data = MemoryUtil.memAlloc(size);
    }

    @Override
    protected void realloc(int newSize) {
        data = MemoryUtil.memRealloc(data, newSize);
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

    @Override
    public long getAddress() {
        return MemoryUtil.memAddress(data);
    }
}

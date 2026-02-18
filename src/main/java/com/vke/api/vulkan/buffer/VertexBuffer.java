package com.vke.api.vulkan.buffer;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class VertexBuffer extends CpuBuffer {
    protected ByteBuffer data;

    public VertexBuffer(int baseVertexCount) {
        super(baseVertexCount);
    }

    public VertexBuffer(int baseVertexCount, int stride) {
        super(baseVertexCount, stride);
    }

    @Override
    protected void alloc(int size) {
        data = MemoryUtil.memAlloc(size);
        data.order(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    protected void realloc(int newSize) {
        data = MemoryUtil.memRealloc(data, newSize);
        data.order(ByteOrder.LITTLE_ENDIAN);
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

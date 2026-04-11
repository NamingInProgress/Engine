package com.vke.api.rendering.vulkan.buffer;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class VertexBuffer extends CpuBuffer {
    protected ByteBuffer data;
    protected VertexByteSink sink;

    public VertexBuffer(int baseVertexCount) {
        super(baseVertexCount);
        this.sink = generateSink();
    }

    public VertexBuffer(int baseVertexCount, boolean allocNow) {
        super(baseVertexCount, allocNow);
        this.sink = generateSink();
    }

    public VertexBuffer(int baseVertexCount, int stride) {
        super(baseVertexCount, stride);
        this.sink = generateSink();
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
        this.sink = generateSink();
    }

    public ByteBuffer getData() {
        return data;
    }

    public abstract int getByteStride();

    protected abstract VertexByteSink generateSink();

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

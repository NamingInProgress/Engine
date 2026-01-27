package com.vke.core.rendering.buffer;

import com.vke.api.vulkan.buffer.CpuBuffer;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

public class IndexBuffer extends CpuBuffer {
    private IntBuffer data;

    public IndexBuffer(int baseCap) {
        super(baseCap);
    }

    protected void alloc(int size) {
        data = MemoryUtil.memAlloc(size).asIntBuffer();
    }

    protected void realloc(int newSizeBytes) {
        data = MemoryUtil.memRealloc(data, newSizeBytes / getByteStride());
    }

    public void put(int... indices) {
        ensureSpace(indices.length);
        data.put(indices);
    }

    @Override
    public int getByteStride() {
        return Integer.BYTES;
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

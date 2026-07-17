package com.vke.core.rendering.vulkan.buffers.premade.ibo;

import com.vke.api.rendering.vulkan.buffer.CpuBuffer;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

public class IndexBuffer extends CpuBuffer {
    protected IntBuffer data;

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
        elementCount += indices.length;
        data.flip();
    }

    public void reset() {
        data.position(0);
        elementCount = 0;
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

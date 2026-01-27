package com.vke.core.rendering.buffer;

import com.vke.api.vulkan.buffer.CpuBuffer;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class GeneralBuffer extends CpuBuffer {
    private ByteBuffer data;
    private long address;
    private int stride;

    public GeneralBuffer(int baseCap, int stride) {
        super(baseCap, false);
        this.stride = stride;
        alloc(baseCap * getByteStride());
    }

    @Override
    protected void alloc(int size) {
        data = MemoryUtil.memAlloc(size);
        address = MemoryUtil.memAddress(data);
    }

    @Override
    protected void realloc(int newSize) {
        data = MemoryUtil.memRealloc(data, newSize);
        address = MemoryUtil.memAddress(data);
    }

    public void putData(byte... data) {
        this.data.put(data);
    }

    public void putDataFloats(float... data) {
        for (float f : data) {
            this.data.putFloat(f);
        }
    }

    public void putDataInts(int... data) {
        for (int i : data) {
            this.data.putInt(i);
        }
    }

    @Override
    public int getByteStride() {
        return stride;
    }

    @Override
    public void free() {
        MemoryUtil.memFree(data);
    }

    public long getAddress() {
        return address;
    }
}

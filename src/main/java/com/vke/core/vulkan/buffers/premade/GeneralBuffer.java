package com.vke.core.vulkan.buffers.premade;

import com.vke.api.rendering.vulkan.buffer.CpuBuffer;
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

    public GeneralBuffer(ByteBuffer data, boolean copy) {
        super(data.capacity(), false);
        this.stride = 1;
        if (copy) {
            alloc(data.capacity());
            MemoryUtil.memCopy(MemoryUtil.memAddress(data), MemoryUtil.memAddress(this.data), data.remaining());
        } else {
            this.data = data;
        }
        this.elementCount = data.remaining();
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

    public ByteBuffer getData() {
        return data.limit(elementCount);
    }
}

package com.vke.api.rendering.vulkan.buffer;

import com.vke.utils.io.Disposable;

public abstract class CpuBuffer implements Disposable {
    public static final double GROWTH_FAC = 1.61803398874989490252573887119069695472717285156250;

    public int elementCount;
    protected int capacity;

    public CpuBuffer(int baseCap) {
        this(baseCap, true);
    }

    public CpuBuffer(int baseCap, int stride) {
        this(baseCap, true, stride);
    }

    public CpuBuffer(int baseCap, boolean allocateNow) {
        capacity = baseCap;
        if (allocateNow) {
            alloc(baseCap * getByteStride());
        }
    }

    public CpuBuffer(int baseCap, boolean allocateNow, int stride) {
        capacity = baseCap;
        if (allocateNow) {
            alloc(baseCap * stride);
        }
    }

    protected abstract void alloc(int size);
    protected abstract void realloc(int newSize);

    public abstract int getByteStride();

    protected void ensureSpace(int n) {
        int newCount = elementCount + n;
        if (newCount > capacity) {
            while (newCount > capacity) {
                capacity = (int) (((double) capacity) * GROWTH_FAC);
            }
            int size = capacity * getByteStride();
            realloc(size);
        }
    }

    public long getSizeBytes() {
        return (long) elementCount * getByteStride();
    }

    public abstract long getAddress();
}

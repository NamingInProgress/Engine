package com.vke.api.vulkan.buffer;

import com.vke.utils.Disposable;

public abstract class CpuBuffer implements Disposable {
    private static final double GROWTH_FAC = 1.61803398874989490252573887119069695472717285156250;

    public int elementCount;
    protected int capacity;

    public CpuBuffer(int baseCap) {
        this(baseCap, true);
    }

    public CpuBuffer(int baseCap, boolean allocateNow) {
        capacity = baseCap;
        if (allocateNow) {
            alloc(baseCap * getByteStride());
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

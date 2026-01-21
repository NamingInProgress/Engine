package com.vke.core.memory;

import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

public class longP implements HeapAllocated<LongBuffer> {
    private LongBuffer heapObject;

    public longP(LongBuffer heapObject) {
        this.heapObject = heapObject;
    }

    @Override
    public LongBuffer getHeapObject() {
        return heapObject;
    }

    @Override
    public void free() {
        MemoryUtil.memFree(heapObject);
    }
}

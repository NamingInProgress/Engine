package com.vke.core.memory;

import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

public class intP implements HeapAllocated<IntBuffer> {
    private IntBuffer heapObject;

    public intP(IntBuffer heapObject) {
        this.heapObject = heapObject;
    }

    @Override
    public IntBuffer getHeapObject() {
        return heapObject;
    }

    @Override
    public void free() {
        MemoryUtil.memFree(heapObject);
    }
}
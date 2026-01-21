package com.vke.core.memory;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class voidP implements HeapAllocated<ByteBuffer> {
    private ByteBuffer heapObject;

    public voidP(ByteBuffer heapObject) {
        this.heapObject = heapObject;
    }

    @Override
    public ByteBuffer getHeapObject() {
        return heapObject;
    }

    @Override
    public void free() {
        MemoryUtil.memFree(heapObject);
    }
}

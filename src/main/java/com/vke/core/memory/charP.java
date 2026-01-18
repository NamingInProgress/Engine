package com.vke.core.memory;

import org.lwjgl.system.MemoryUtil;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public class charP implements HeapAllocated<ByteBuffer> {
    private ByteBuffer heapObject;

    public charP(ByteBuffer heapObject) {
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

package com.vke.core.memory;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;

import java.io.Closeable;
import java.nio.ByteBuffer;

public class charPP implements HeapAllocated<PointerBuffer> {
    private PointerBuffer heapObject;
    private int used, size;
    
    public charPP(PointerBuffer heapObject, int size) {
        this.heapObject = heapObject;
        this.used = 0;
        this.size = size;
    }

    @Override
    public PointerBuffer getHeapObject() {
        return heapObject;
    }

    public void utf8(CharSequence sqn) {
        if (used >= size) {
            throw new RuntimeException("Tried to allocate string, but the array is too small!");
        }
        ByteBuffer string = MemoryUtil.memUTF8(sqn);
        heapObject.put(string);
        used++;
    }
    
    @Override
    public void free() {
        for (int i = 0; i < used; i++) {
            MemoryUtil.nmemFree(heapObject.get(i));
        }
        MemoryUtil.memFree(heapObject);
    }
}

package com.vke.core.memory;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class CStringArray implements AutoCloseable {
    private PointerBuffer heapObject;
    private int used, size;
    
    public CStringArray(PointerBuffer heapObject, int size) {
        this.heapObject = heapObject;
        this.used = 0;
        this.size = size;
    }

    public void utf8(CharSequence sqn) {
        if (used >= size) {
            throw new RuntimeException("Tried to allocate string, but the array is too small!");
        }
        ByteBuffer string = MemoryUtil.memUTF8(sqn);
        heapObject.put(used++, string);
    }
    
    @Override
    public void close() {
        for (int i = 0; i < used; i++) {
            MemoryUtil.nmemFree(heapObject.get(i));
        }
        MemoryUtil.memFree(heapObject);
    }
}

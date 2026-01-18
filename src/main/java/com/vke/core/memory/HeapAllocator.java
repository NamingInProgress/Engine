package com.vke.core.memory;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;
import java.util.ArrayList;

public class HeapAllocator implements AutoCloseable {

    private final ArrayList<AutoCloseable> objects = new ArrayList<>();

    public CStringArray strings(int amount) {
        CStringArray arr = new CStringArray(MemoryUtil.memAllocPointer(amount), amount);
        objects.add(arr);
        return arr;
    }

    @Override
    public void close() {
        objects.forEach((c) -> {
            try {
                c.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}

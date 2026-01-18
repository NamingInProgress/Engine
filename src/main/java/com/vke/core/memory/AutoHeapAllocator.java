package com.vke.core.memory;

import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;

public class AutoHeapAllocator implements AutoCloseable {

    private final ArrayList<HeapAllocated<?>> objects = new ArrayList<>();

    public charPP charPP(int amount) {
        charPP arr = new charPP(MemoryUtil.memAllocPointer(amount), amount);
        objects.add(arr);
        return arr;
    }

    public charP utf8(CharSequence sqn) {
        charP s = new charP(MemoryUtil.memUTF8(sqn));
        objects.add(s);
        return s;
    }

    @Override
    public void close() {
        objects.forEach((c) -> {
            try {
                c.free();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}

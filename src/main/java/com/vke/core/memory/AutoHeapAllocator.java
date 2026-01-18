package com.vke.core.memory;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Struct;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.function.Function;

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

    public voidP alloc(int size) {
        voidP v = new voidP(MemoryUtil.memAlloc(size));
        objects.add(v);
        return v;
    }

    public <T> T allocStruct(int size, Function<ByteBuffer, T> creator) {
        voidP container = alloc(size);
        return creator.apply(container.getHeapObject());
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

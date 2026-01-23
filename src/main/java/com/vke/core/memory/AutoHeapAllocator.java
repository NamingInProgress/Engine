package com.vke.core.memory;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
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

    public intP allocInt(int size) {
        intP v = new intP(MemoryUtil.memAllocInt(size));
        objects.add(v);
        return v;
    }

    public intP ints(int... ints) {
        intP v = new intP(MemoryUtil.memAllocInt(ints.length));
        IntBuffer heap = v.getHeapObject();
        for (int i : ints) {
            heap.put(i);
        }
        heap.flip();
        objects.add(v);
        return v;
    }

    public longP allocLong(int size) {
        longP v = new longP(MemoryUtil.memAllocLong(size));
        objects.add(v);
        return v;
    }

    public longP longs(long... longs) {
        longP v = new longP(MemoryUtil.memAllocLong(longs.length));
        LongBuffer heap = v.getHeapObject();
        for (long i : longs) {
            heap.put(i);
        }
        heap.flip();
        objects.add(v);
        return v;
    }

    public voidP bytes(byte... bytes) {
        voidP v = new voidP(MemoryUtil.memAlloc(bytes.length));
        ByteBuffer heap = v.getHeapObject();
        for (byte b : bytes) {
            heap.put(b);
        }
        heap.flip();
        objects.add(v);
        return v;
    }

    public <T> T allocStruct(int size, Function<ByteBuffer, T> creator) {
        voidP container = alloc(size);
        return creator.apply(container.getHeapObject());
    }

    public <T> T allocBuffer(int size, int n, Function<ByteBuffer, T> creator) {
        voidP container = alloc(size * n);
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

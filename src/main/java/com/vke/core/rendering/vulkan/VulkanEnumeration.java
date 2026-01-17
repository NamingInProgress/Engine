package com.vke.core.rendering.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.function.Function;

public class VulkanEnumeration<T, B> implements Iterable<T> {
    private int count;
    private PointerBuffer pointerBuffer;
    private Creator<T> creator;

    public VulkanEnumeration(MemoryStack stack, Creator<T> creator, FnCall call) {
        this.creator = creator;
        IntBuffer pCount = stack.mallocInt(1);
        call.call(pCount, null);
        count = pCount.get(0);
        pointerBuffer = stack.mallocPointer(count);
        call.call(pCount, pointerBuffer);
    }

    @Override
    public Iterator<T> iterator() {
        return new I();
    }

    public class I implements Iterator<T> {
        private int index;

        @Override
        public boolean hasNext() {
            return index < count;
        }

        @Override
        public T next() {
            long handle = pointerBuffer.get(index++);
            return creator.create(handle);
        }
    }

    public interface Creator<R> {
        R create(long handle);
    }

    public interface FnCall {
        void call(IntBuffer pCount, PointerBuffer pData);
    }
}

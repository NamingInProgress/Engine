package com.vke.core.rendering.vertexconsumer;

import com.vke.api.rendering.vulkan.buffer.CpuBuffer;
import com.vke.utils.iter.Iter;
import com.vke.utils.iter.helpers.OfArray;

import java.util.Arrays;
import java.util.function.Supplier;

public class RecyclerArrayList<T> {
    private T[] array;
    private int cap, len;

    @SafeVarargs
    public RecyclerArrayList(int cap, T... ignore) {
        this.array = Arrays.copyOf(ignore, cap);
        this.cap = cap;
        this.len = 0;
    }

    private void ensureSpace(int expectedElems) {
        while (len + expectedElems > cap) {
            double fac = CpuBuffer.GROWTH_FAC;
            cap = (int) (fac * (double) cap);
        }
        array = Arrays.copyOf(array, cap);
    }

    public void add(T vertex) {
        ensureSpace(1);
        array[len++] = vertex;
    }

    public void add(T... vertices) {
        ensureSpace(vertices.length);
        System.arraycopy(vertices, 0, array, len, vertices.length);
        len += vertices.length;
    }

    public void clear() {
        len = 0;
    }

    public T[] toArray() {
        return Arrays.copyOf(array, len);
    }

    public Iter<T> iter() {
        return new OfArray<>(array, 0, len);
    }

    public T get(int index) {
        if(index >= cap) return null;
        return array[index];
    }

    public T lastUnchecked() {
        return array[len - 1];
    }

    public int len() {
        return len;
    }

    public void virtualAdd() {
        len++;
    }

    public boolean wasVeryLastElement() {
        return len >= cap;
    }

    public T getOrCreateElement(boolean checkRecycle, Supplier<T> factory) {
        if (!wasVeryLastElement() && checkRecycle) {
            T newBatch = get(len());
            if (newBatch == null) {
                return getOrCreateElement(false, factory);
            }
            this.virtualAdd();
            return newBatch;
        } else {
            T newBatch = factory.get();
            add(newBatch);
            return newBatch;
        }
    }

    public T getOrCreateElement(int index, boolean checkRecycle, Supplier<T> factory) {
        if (index >= cap && checkRecycle) {
            T newBatch = get(len());
            if (newBatch == null) {
                return getOrCreateElement(index, false, factory);
            }
            this.virtualAdd();
            return newBatch;
        } else {
            T newBatch = factory.get();
            add(newBatch);
            return newBatch;
        }
    }
}

package com.vke.utils.collection;

import java.util.Iterator;

public class IdxArrayIter<T> implements Iterator<T> {
    private final T[] array;
    private final Iterator<Integer> indices;

    public IdxArrayIter(T[] array, Iterator<Integer> indices) {
        this.array = array;
        this.indices = indices;
    }

    @Override
    public boolean hasNext() {
        return indices.hasNext();
    }

    @Override
    public T next() {
        return array[indices.next()];
    }
}

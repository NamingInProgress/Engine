package com.vke.utils.collection;

import java.util.Iterator;

public class ArrayIter<T> implements Iterator<T> {
    private final T[] array;
    private final int length;
    private int index;

    public ArrayIter(T[] array) {
        this.array = array;
        this.length = array.length;
    }

    @Override
    public boolean hasNext() {
        return index < length;
    }

    @Override
    public T next() {
        return array[index++];
    }
}

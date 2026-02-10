package com.vke.utils.iter.helpers;

import com.vke.utils.iter.Iter;

import java.util.Iterator;

public class IteratorAdapter<T> implements Iterator<T> {
    private final Iter<T> parent;
    private Option<T> cache;

    public IteratorAdapter(Iter<T> parent) {
        this.parent = parent;
        cache = parent.next();
    }

    @Override
    public boolean hasNext() {
        return cache.isSome();
    }

    @Override
    public T next() {
        T v = cache.unwrap();
        cache = parent.next();
        return v;
    }
}

package com.vke.utils.collection;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class IterAble<T> implements Iterable<T> {
    private Iterator<T> i;

    public IterAble(Iterator<T> i) {
        this.i = i;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return i;
    }
}

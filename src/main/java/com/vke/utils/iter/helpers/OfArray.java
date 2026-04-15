package com.vke.utils.iter.helpers;

import com.vke.utils.iter.Iter;
import org.jetbrains.annotations.NotNull;

public class OfArray<T> implements Iter<T> {
    private final T[] array;
    private int index, limit;

    public OfArray(T[] array) {
        this.array = array;
        this.limit = array.length;
    }

    public OfArray(T[] array, int off, int len) {
        this.array = array;
        this.index = off;
        this.limit = len;
    }

    @Override
    public @NotNull Option<T> next() {
        if (index >= limit) {
            return Option.none();
        }
        return Option.some(array[index++]);
    }
}

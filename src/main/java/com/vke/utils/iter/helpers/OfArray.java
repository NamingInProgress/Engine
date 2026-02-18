package com.vke.utils.iter.helpers;

import com.vke.utils.iter.Iter;
import org.jetbrains.annotations.NotNull;

public class OfArray<T> implements Iter<T> {
    private final T[] array;
    private int index;

    public OfArray(T[] array) {
        this.array = array;
    }

    @Override
    public @NotNull Option<T> next() {
        if (index >= array.length) {
            return Option.none();
        }
        return Option.some(array[index++]);
    }
}

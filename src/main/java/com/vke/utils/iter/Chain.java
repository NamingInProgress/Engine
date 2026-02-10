package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

public class Chain<T> implements Iter<T> {
    private Iter<T> current;
    private final Iter<T> next;

    public Chain(Iter<T> first, Iter<T> second) {
        this.current = first;
        this.next = second;
    }

    @Override
    public @NotNull Option<T> next() {
        Option<T> val = current.next();
        if (val.isSome()) return val;

        current = next;
        return current.next();
    }
}
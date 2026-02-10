package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

public class Take<T> implements Iter<T> {
    private final Iter<T> parent;
    private int remaining;

    public Take(Iter<T> parent, int n) {
        this.parent = parent;
        this.remaining = n;
    }

    @Override
    public @NotNull Option<T> next() {
        if (remaining <= 0) return Option.none();
        remaining--;

        return parent.next();
    }
}
package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

public class Skip<T> implements Iter<T> {
    private final Iter<T> parent;
    private int remaining;

    public Skip(Iter<T> parent, int n) {
        this.parent = parent;
        this.remaining = n;
    }

    @Override
    public @NotNull Option<T> next() {
        while (remaining > 0) {
            if (parent.next().isNone()) return Option.none();
            remaining--;
        }
        return parent.next();
    }
}

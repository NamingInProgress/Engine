package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

public class Peekable<T> implements Iter<T> {
    private final Iter<T> parent;
    private Option<T> peeked = Option.none();

    public Peekable(Iter<T> parent) {
        this.parent = parent;
    }

    public @NotNull Option<T> peek() {
        if (peeked.isNone()) {
            peeked = parent.next();
        }
        return peeked;
    }

    @Override
    public @NotNull Option<T> next() {
        if (peeked.isSome()) {
            Option<T> out = peeked;
            peeked = Option.none();
            return out;
        }
        return parent.next();
    }
}
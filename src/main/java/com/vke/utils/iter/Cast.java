package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

public class Cast<T, U> implements Iter<U> {
    private final Iter<T> parent;
    private final U[] ignore;

    public Cast(Iter<T> parent, U... ignore) {
        this.parent = parent;
        this.ignore = ignore;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Option<U> next() {
        return parent.next().map(x -> (U) x);
    }
}

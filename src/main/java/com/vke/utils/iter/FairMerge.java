package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

public class FairMerge<T> implements Iter<T> {
    private final Iter<T> a, b;
    private boolean side;

    public FairMerge(Iter<T> a, Iter<T> b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public @NotNull Option<T> next() {
        side ^= true;
        return side ? a.next() : b.next();
    }
}

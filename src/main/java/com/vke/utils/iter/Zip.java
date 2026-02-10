package com.vke.utils.iter;

import com.vke.utils.Pair;
import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

public class Zip<T, U> implements Iter<Pair<T, U>> {
    private final Iter<T> a;
    private final Iter<U> b;

    public Zip(Iter<T> a, Iter<U> b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public @NotNull Option<Pair<T, U>> next() {
        Option<T> ta = a.next();
        Option<U> tb = b.next();

        if (ta.isNone() || tb.isNone()) return Option.none();

        return Option.some(new Pair<>(ta.unwrap(), tb.unwrap()));
    }
}
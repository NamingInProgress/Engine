package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class Filter<T> implements Iter<T> {
    private final Iter<T> parent;
    private final Predicate<T> filter;

    public Filter(Iter<T> parent, Predicate<T> filter) {
        this.parent = parent;
        this.filter = filter;
    }

    @Override
    public @NotNull Option<T> next() {
        while(true) {
            Option<T> next = parent.next();
            if (next.isNone()) return next;
            T val = next.unwrap();
            if (filter.test(val)) {
                return Option.some(val);
            }
        }
    }
}

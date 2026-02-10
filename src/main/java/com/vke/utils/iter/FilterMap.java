package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class FilterMap<T, U> implements Iter<U> {
    private final Iter<T> parent;
    private final Function<T, Option<U>> mapper;

    public FilterMap(Iter<T> parent, Function<T, Option<U>> mapper) {
        this.parent = parent;
        this.mapper = mapper;
    }

    @Override
    public @NotNull Option<U> next() {
        while (true) {
            Option<T> next = parent.next();
            if (next.isNone()) return Option.none();

            Option<U> mapped = mapper.apply(next.unwrap());
            if (mapped.isSome()) return mapped;
        }
    }
}
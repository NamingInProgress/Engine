package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class Map<T, U> implements Iter<U> {
    private final Iter<T> parent;
    private final Function<T, U> mapper;

    public Map(Iter<T> parent, Function<T, U> mapper) {
        this.parent = parent;
        this.mapper = mapper;
    }

    @Override
    public @NotNull Option<U> next() {
        return parent.next().map(mapper);
    }
}
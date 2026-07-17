package com.vke.utils.iter;

import com.vke.utils.functionalinterface.FaultyFunction;
import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

public class FaultyMap<T, U, E extends Throwable> implements Iter<U> {
    private final Iter<T> parent;
    private final FaultyFunction<T, U, E> mapper;

    public FaultyMap(Iter<T> parent, FaultyFunction<T, U, E> mapper) {
        this.parent = parent;
        this.mapper = mapper;
    }

    @Override
    public @NotNull Option<U> next() {
        try {
            return parent.next().faultyMap(mapper);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
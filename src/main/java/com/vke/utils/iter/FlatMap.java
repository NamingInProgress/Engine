package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class FlatMap<T, U> implements Iter<U> {
    private final Iter<T> parent;
    private final Function<T, Iter<U>> mapper;
    private Iter<U> current = null;

    public FlatMap(Iter<T> parent, Function<T, Iter<U>> mapper) {
        this.parent = parent;
        this.mapper = mapper;
    }

    @Override
    public @NotNull Option<U> next() {
        while (true) {
            if (current != null) {
                Option<U> next = current.next();
                if (next.isSome()) return next;
                current = null;
            }

            Option<T> nextParent = parent.next();
            if (nextParent.isNone()) return Option.none();

            current = mapper.apply(nextParent.unwrap());
        }
    }
}
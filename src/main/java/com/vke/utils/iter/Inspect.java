package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class Inspect<T> implements Iter<T> {
    private final Iter<T> parent;
    private final Consumer<T> inspector;

    public Inspect(Iter<T> parent, Consumer<T> inspector) {
        this.parent = parent;
        this.inspector = inspector;
    }

    @Override
    public @NotNull Option<T> next() {
        Option<T> next = parent.next();
        if (next.isSome()) {
            inspector.accept(next.unwrap());
        }
        return next;
    }
}

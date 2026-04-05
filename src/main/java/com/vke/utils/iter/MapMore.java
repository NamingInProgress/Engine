package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MapMore<T, U> implements Iter<U> {
    private final Iter<T> parent;
    private final BiConsumer<T, Consumer<U>> factory;
    private final ArrayDeque<U> buffer;

    public MapMore(Iter<T> parent, BiConsumer<T, Consumer<U>> factory) {
        this.parent = parent;
        this.factory = factory;
        this.buffer = new ArrayDeque<>();
    }

    @Override
    public @NotNull Option<U> next() {
        if (!buffer.isEmpty()) {
            return Option.some(buffer.removeFirst());
        }
        Option<T> n = parent.next();
        if (n.isNone()) return Option.none();
        T next = n.unwrap();
        factory.accept(next, buffer::addLast);
        return next();
    }
}

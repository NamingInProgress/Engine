package com.vke.utils.iter;

import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

public class Enumerate<T> implements Iter<ObjectCursor<T>> {
    private final Iter<T> parent;
    private int index = 0;

    public Enumerate(Iter<T> parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull Option<ObjectCursor<T>> next() {
        Option<T> next = parent.next();
        if (next.isNone()) return Option.none();

        ObjectCursor<T> cursor = new ObjectCursor<>();
        cursor.index = index++;
        cursor.value = next.unwrap();
        return Option.some(cursor);
    }
}
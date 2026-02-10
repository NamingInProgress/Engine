package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Cycle<T> implements Iter<T> {
    private final Iter<T> parent;
    private final List<T> buffer = new ArrayList<>();

    private int index = 0;
    private boolean exhausted = false;

    public Cycle(Iter<T> parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull Option<T> next() {
        if (!exhausted) {
            Option<T> next = parent.next();
            if (next.isSome()) {
                T val = next.unwrap();
                buffer.add(val);
                return Option.some(val);
            }

            exhausted = true;

            if (buffer.isEmpty()) {
                return Option.none();
            }
        }

        T val = buffer.get(index++);
        if (index >= buffer.size()) index = 0;

        return Option.some(val);
    }
}


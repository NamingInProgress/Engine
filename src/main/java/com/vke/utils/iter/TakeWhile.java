package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class TakeWhile<T> implements Iter<T> {
    private final Iter<T> parent;
    private final Predicate<T> pred;
    private boolean done = false;

    public TakeWhile(Iter<T> parent, Predicate<T> pred) {
        this.parent = parent;
        this.pred = pred;
    }

    @Override
    public @NotNull Option<T> next() {
        if (done) return Option.none();

        Option<T> next = parent.next();
        if (next.isNone()) {
            done = true;
            return next;
        }

        T val = next.unwrap();
        if (!pred.test(val)) {
            done = true;
            return Option.none();
        }

        return Option.some(val);
    }
}
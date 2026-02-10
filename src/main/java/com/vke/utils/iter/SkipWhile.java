package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class SkipWhile<T> implements Iter<T> {
    private final Iter<T> parent;
    private final Predicate<T> pred;
    private boolean skipping = true;

    public SkipWhile(Iter<T> parent, Predicate<T> pred) {
        this.parent = parent;
        this.pred = pred;
    }

    @Override
    public @NotNull Option<T> next() {
        while (true) {
            Option<T> next = parent.next();
            if (next.isNone()) return Option.none();

            T val = next.unwrap();
            if (!skipping || !pred.test(val)) {
                skipping = false;
                return Option.some(val);
            }
        }
    }
}
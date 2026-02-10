package com.vke.utils.iter.helpers;

import com.vke.utils.iter.Iter;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

public final class SpliteratorAdapter<T> extends Spliterators.AbstractSpliterator<T> {
    private final Iter<T> iter;

    public SpliteratorAdapter(Iter<T> iter) {
        super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL);
        this.iter = iter;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        Option<T> next = iter.next();
        if (next.isNone()) return false;
        action.accept(next.unwrap());
        return true;
    }
}
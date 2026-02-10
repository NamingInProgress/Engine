package com.vke.utils.iter.helpers;

import com.vke.utils.iter.Iter;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class OfIterator<T> implements Iter<T> {
    private final Iterator<T> parent;

    public OfIterator(Iterator<T> parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull Option<T> next() {
        return parent.hasNext() ? Option.some(parent.next()) : Option.none();
    }
}

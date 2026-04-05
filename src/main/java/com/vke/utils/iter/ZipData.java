package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import com.vke.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ZipData<T, D> implements Iter<Pair<T, D>> {
    private final Iter<T> parent;
    private final Function<T, D> extractor;

    public ZipData(Iter<T> parent, Function<T, D> extractor) {
        this.parent = parent;
        this.extractor = extractor;
    }

    @Override
    public @NotNull Option<Pair<T, D>> next() {
        return parent.next().map(x -> new Pair<>(x, extractor.apply(x)));
    }
}

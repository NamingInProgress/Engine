package com.vke.utils.iter;

import com.vke.utils.fi.FaultyRunnable;
import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

public class FaultyFinisher<T> implements Iter<T> {
    private final Iter<T> parent;
    private final FaultyRunnable finishRunnable;
    private boolean finished = false;

    public FaultyFinisher(Iter<T> parent, FaultyRunnable finishRunnable) {
        this.parent = parent;
        this.finishRunnable = finishRunnable;
    }

    @Override
    public @NotNull Option<T> next() {
        Option<T> next = parent.next();

        if (next.isNone() && !finished) {
            finished = true;
            try {
                finishRunnable.run();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        return next;
    }
}
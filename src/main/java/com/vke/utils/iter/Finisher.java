package com.vke.utils.iter;

import com.vke.utils.iter.helpers.Option;
import org.jetbrains.annotations.NotNull;

public class Finisher<T> implements Iter<T> {
    private final Iter<T> parent;
    private final Runnable finishRunnable;
    private boolean finished = false;

    public Finisher(Iter<T> parent, Runnable finishRunnable) {
        this.parent = parent;
        this.finishRunnable = finishRunnable;
    }

    @Override
    public @NotNull Option<T> next() {
        Option<T> next = parent.next();

        if (next.isNone() && !finished) {
            finished = true;
            finishRunnable.run();
        }

        return next;
    }
}

package com.vke.api.utils;

import java.util.Iterator;

public interface NotifyingIterable<T> extends Iterable<T> {
    boolean hasNext();
    T next();

    /**
     * Notifies the underlying implementation that the iteration is completed
     */
    void notifyEnd();

    @Override
    default Iterator<T> iterator() {
        return new JavaIterBridge<>(this);
    }

    class JavaIterBridge<T> implements Iterator<T> {
        private boolean called;
        private final NotifyingIterable<T> iter;

        private JavaIterBridge(NotifyingIterable<T> iter) {
            this.iter = iter;
        }

        @Override
        public boolean hasNext() {
            boolean hasNext = iter.hasNext();
            if (!hasNext && !called) {
                called = true;
                iter.notifyEnd();
            }
            return hasNext;
        }

        @Override
        public T next() {
            return iter.next();
        }
    }
}

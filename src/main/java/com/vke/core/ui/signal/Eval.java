package com.vke.core.ui.signal;

@FunctionalInterface
public interface Eval<T> {
    T generate();
}

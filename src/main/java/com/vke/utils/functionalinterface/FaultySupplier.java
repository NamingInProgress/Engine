package com.vke.utils.functionalinterface;

public interface FaultySupplier<T, E extends Throwable> {
    T get() throws E;
}

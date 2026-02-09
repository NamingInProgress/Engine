package com.vke.utils.fi;

public interface FaultySupplier<T, E extends Throwable> {
    T get() throws E;
}

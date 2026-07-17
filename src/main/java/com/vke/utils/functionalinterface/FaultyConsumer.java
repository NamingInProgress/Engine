package com.vke.utils.functionalinterface;

public interface FaultyConsumer<T, E extends Throwable> {
    void accept(T t) throws E;
}

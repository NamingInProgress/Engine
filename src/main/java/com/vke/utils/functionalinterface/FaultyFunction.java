package com.vke.utils.functionalinterface;

@FunctionalInterface
public interface FaultyFunction<T, U, E extends Throwable> {
    U apply(T t) throws E;
}

package com.vke.utils.functionalinterface;

@FunctionalInterface
public interface Thenable<T> {

    T andThen(Runnable runnable);

}

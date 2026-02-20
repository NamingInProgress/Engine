package com.vke.utils.fi;

@FunctionalInterface
public interface Thenable<T> {

    T andThen(Runnable runnable);

}

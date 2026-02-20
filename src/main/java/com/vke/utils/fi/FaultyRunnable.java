package com.vke.utils.fi;

@FunctionalInterface
public interface FaultyRunnable {
    void run() throws Throwable;
}

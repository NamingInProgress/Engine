package com.vke.utils.functionalinterface;

@FunctionalInterface
public interface FaultyRunnable {
    void run() throws Exception;
}

package com.vke.utils;

public class Stopwatch {
    private long time;
    private long start;

    public Stopwatch() {
        this.time = 0;
    }

    public void reset() {
        this.time = 0;
    }

    public void start() {
        this.start = System.currentTimeMillis();
    }

    public void stop() {
        this.time += (System.currentTimeMillis() - start);
    }

    public long getMillis() {
        return time;
    }
}

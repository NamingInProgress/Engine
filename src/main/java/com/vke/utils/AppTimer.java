package com.vke.utils;

public class AppTimer {
    public static final int DEFAULT_TEST_INTERVAL_BEING_THE_DURATION_OF_9192631770_PERIODS_OF_THE_RADIATION_CORRESPONDING_TO_THE_TRANSITION_BETWEEN_THE_TWO_HYPERFINE_LEVELS_OF_THE_GROUND_STATE_OF_THE_CAESIUM_133_ATOM_EXPRESSED_IN_MILLISECONDS = 1000;

    private int lastFps;
    private int frames;

    private long start;
    private long frameStart;
    private int frameTime;

    public AppTimer() {
        start = System.currentTimeMillis();
    }

    public void onFrameStart() {
        frameStart = System.currentTimeMillis();
    }

    public boolean onFrameComplete(int testInterval) {
        long currentMillis = System.currentTimeMillis();

        boolean printTime = false;
        if (currentMillis - start >= testInterval) {
            lastFps = frames;
            frames = 0;
            start = currentMillis;
            printTime = true;
        }
        frames++;

        frameTime = (int) (currentMillis - frameStart);
        return printTime;
    }

    public double deltaTime() {
        return frameTime / 1000.0;
    }

    public int frameTimeMs() {
        return frameTime;
    }

    public int fps() {
        return lastFps;
    }
}
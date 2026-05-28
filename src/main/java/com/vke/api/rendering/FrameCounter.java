package com.vke.api.rendering;

public class FrameCounter {

    private final int FRAMES_IN_FLIGHT;

    private int frameIndex;
    private long frame;

    public FrameCounter(int framesInFlight) {
        this.FRAMES_IN_FLIGHT = framesInFlight;
    }

    public void nextFrame() {
        frameIndex = (frameIndex + 1) % FRAMES_IN_FLIGHT;
        frame++;
    }

    public int framesInFlight() { return this.FRAMES_IN_FLIGHT; }
    public int currentIndex() { return this.frameIndex; }
    public long currentFrame() { return this.frame; }

}

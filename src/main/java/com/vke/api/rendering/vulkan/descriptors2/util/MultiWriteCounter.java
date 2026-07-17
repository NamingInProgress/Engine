package com.vke.api.rendering.vulkan.descriptors2.util;

public class MultiWriteCounter {

    private final int maxRotations;

    private int currentRotation;

    public MultiWriteCounter(int maxRotations) {
        this(maxRotations, 0);
    }

    public MultiWriteCounter(int maxRotations, int startRotation) {
        this.maxRotations = maxRotations;
        this.currentRotation = startRotation;
    }

    public boolean advance() {
        currentRotation++;

        return currentRotation <= maxRotations;

        //if (currentRotation > maxRotations) throw new IllegalStateException("Rotated out of bounds (max rotations: " + maxRotations + ")");
    }

    public void reset() {
        this.currentRotation = 0;
    }

    public int getCurrentRotation() {
        return this.currentRotation;
    }

    public int getMaxRotations() {
        return maxRotations;
    }
}

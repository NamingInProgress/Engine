package com.vke.core.file.png.adam7;

public class SubImage {
    private final int x, y;
    private byte[][] samples;

    public SubImage(int x, int y, int pixelStride) {
        this.x = x;
        this.y = y;
        this.samples = new byte[8 * 8][pixelStride];
    }

    public byte[] getPixel(int index) {
        return samples[index];
    }
}

package com.vke.core.file.gzip.deflate.lz77;

public class SlidingWindow {
    private final byte[] buffer;
    private int seam;
    private int bufferOccupied;

    public SlidingWindow(int byteSize) {
        this.buffer = new byte[byteSize];
    }

    public void append(byte b) {
        buffer[seam] = b;
        seam = (seam + 1) % buffer.length;
        bufferOccupied = Math.min(bufferOccupied + 1, buffer.length);
    }

    public byte backtrack(int distance) {
        if (distance <= 0 || distance > bufferOccupied) {
            //silent fail cuz this should never happen
            return 0;
        }

        //-1 cuz the seam has been incremented already
        int index = seam - distance;
        if (index < 0) {
            index += buffer.length;
        }
        return buffer[index];
    }
}

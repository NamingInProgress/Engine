package com.vke.core.file.deflate;

import com.vke.core.file.deflate.check.Checksum32;
import com.vke.core.file.deflate.exc.InflatingException;
import com.vke.core.file.deflate.lz77.SlidingWindow;
import com.vke.core.file.io.bit.BitInputStream;
import com.vke.core.file.io.bit.ShittyBitInputStream;

import java.io.IOException;
import java.io.InputStream;

public class InflatingDevice {
    private static final int SLIDING_WINDOW_SIZE = 32768;

    private final BitInputStream bitStream;
    private DeflateBlock currentBlock;
    private boolean finished;
    private Checksum32 checksum;
    private SlidingWindow window;

    private int windowSize;

    public InflatingDevice(Checksum32 checksum, InputStream toBeInflated) {
        this.checksum = checksum;
        this.bitStream = new ShittyBitInputStream(toBeInflated);
        this.windowSize = SLIDING_WINDOW_SIZE;
        this.window = new SlidingWindow(windowSize);
    }

    public InflatingDevice(Checksum32 checksum, InputStream toBeInflated, int windowSize) {
        this.checksum = checksum;
        this.bitStream = new ShittyBitInputStream(toBeInflated);
        this.windowSize = windowSize;
        this.window = new SlidingWindow(windowSize);
    }

    public InflatingDevice(Checksum32 checksum, BitInputStream bitStream) {
        this.checksum = checksum;
        this.bitStream = bitStream;
        this.windowSize = SLIDING_WINDOW_SIZE;
        this.window = new SlidingWindow(windowSize);
    }

    public InflatingDevice(Checksum32 checksum, BitInputStream bitStream, int windowSize) {
        this.checksum = checksum;
        this.bitStream = bitStream;
        this.windowSize = windowSize;
        this.window = new SlidingWindow(windowSize);
    }

    /**
     * @return 0–255 for a byte, or -1 when inflation is finished
     */
    public int inflateNextByte() throws InflatingException {
        if (finished) {
            return -1;
        }

        try {
            while (true) {
                if (currentBlock == null) {
                    currentBlock = DeflateBlock.createNextBlock(bitStream, window);
                }

                int value = currentBlock.nextByte(bitStream);

                if (value != -1) {
                    if (checksum != null) {
                        checksum.nextByte(value);
                    }
                    return value;
                }

                if (currentBlock.bFinal()) {
                    finished = true;
                    return -1;
                }

                currentBlock = null;
            }
        } catch (IOException e) {
            throw new InflatingException("Inflation failed", e);
        }
    }

    public boolean isFinished() {
        return finished;
    }
}

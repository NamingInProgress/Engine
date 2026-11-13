package com.vke.core.file.deflate.decompress;

import com.vke.core.file.deflate.decompress.check.Checksum32;
import com.vke.core.file.deflate.exc.InflatingException;
import com.vke.core.file.deflate.decompress.lz77.SlidingWindow;
import com.vke.core.file.io.bit.input.BitInputStream;
import com.vke.core.file.io.bit.input.BasicBitInputStream;

import java.io.IOException;
import java.io.InputStream;

public class InflatingDevice {
    public static final int SLIDING_WINDOW_SIZE = 32768;

    private final BitInputStream bitStream;
    private DeflateBlock currentBlock;
    private boolean finished;
    private Checksum32 checksum;
    private SlidingWindow window;

    private int windowSize;

    public InflatingDevice(Checksum32 checksum, InputStream toBeInflated) {
        this.checksum = checksum;
        this.bitStream = new BasicBitInputStream(toBeInflated);
        this.windowSize = SLIDING_WINDOW_SIZE;
        this.window = new SlidingWindow(windowSize);
    }

    public InflatingDevice(Checksum32 checksum, InputStream toBeInflated, int windowSize) {
        this.checksum = checksum;
        this.bitStream = new BasicBitInputStream(toBeInflated);
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

    public boolean isFinished() {
        return finished;
    }

    public int inflate(byte[] dst, int start, int length) throws InflatingException {
        if (finished) {
            return -1;
        }

        try {
            int decoded = 0;
            while (true) {
                if (currentBlock == null) {
                    currentBlock = DeflateBlock.createNextBlock(bitStream, window);
                }

                decoded += currentBlock.inflateBytes(bitStream, dst, start + decoded, length, checksum);
                if (decoded < length) {
                    if (currentBlock.bFinal()) {
                        finished = true;
                        return decoded;
                    } else {
                        currentBlock = null;
                    }
                } else {
                    return decoded;
                }
            }
        } catch (IOException e) {
            throw new InflatingException("Inflation failed because of IO:" + e.getMessage());
        }
    }
}

package com.vke.core.file.deflate.decompress;

import com.vke.core.file.deflate.decompress.check.Checksum32;
import com.vke.core.file.deflate.decompress.lz77.SlidingWindow;
import com.vke.core.file.deflate.exc.InflatingException;
import com.vke.core.file.io.bit.input.BitInputStream;
import com.vke.core.file.io.bit.input.FastBitInputStream;

import java.io.IOException;
import java.io.InputStream;

public class InflatingDevice {
    public static final int SLIDING_WINDOW_SIZE = 32768;

    private final BitInputStream bitStream;
    private final Checksum32 checksum;
    private final SlidingWindow window;
    private final int windowSize;

    private DeflateBlock currentBlock;
    private boolean finished;

    public InflatingDevice(Checksum32 checksum, BitInputStream bitStream, int windowSize) {
        this.checksum = checksum;
        this.bitStream = bitStream;
        this.windowSize = windowSize;
        this.window = new SlidingWindow(windowSize);
    }

    public InflatingDevice(Checksum32 checksum, BitInputStream bitStream) {
        this(checksum, bitStream, SLIDING_WINDOW_SIZE);
    }

    public InflatingDevice(Checksum32 checksum, InputStream toBeInflated, int windowSize) {
        this(checksum, FastBitInputStream.withLimitRespect(toBeInflated), windowSize);
    }

    public InflatingDevice(Checksum32 checksum, InputStream toBeInflated) {
        this(checksum, toBeInflated, SLIDING_WINDOW_SIZE);
    }

    public boolean isFinished() {
        return finished;
    }

    public int inflate(byte[] dst, int start, int length) throws InflatingException {
        if (finished) {
            return -1;
        }

        try {
            if (currentBlock == null) {
                currentBlock = DeflateBlock.createNextBlock(bitStream, window);
            }

            int decoded = currentBlock.inflateBytes(bitStream, dst, start, length, checksum);

            if (decoded > 0) {
                if (currentBlock.isFinished()) {
                    if (currentBlock.bFinal()) {
                        finished = true;
                    } else {
                        currentBlock = null;
                    }
                }
                return decoded;
            }

            if (decoded == -1 || currentBlock.isFinished()) {
                if (currentBlock.bFinal()) {
                    System.out.println("FINISHED SET TO TRUE FROM BELOW");
                    finished = true;
                    return -1;
                } else {
                    currentBlock = null;
                    return 0;
                }
            }

            return decoded;
        } catch (IOException e) {
            throw new InflatingException("Inflation failed because of IO: " + e.getMessage());
        }
    }
}
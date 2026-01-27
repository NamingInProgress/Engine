package com.vke.core.file.gzip.deflate;

import com.vke.core.file.gzip.deflate.exc.InflatingException;
import com.vke.core.file.gzip.io.bit.BitInputStream;
import com.vke.core.file.gzip.io.bit.GoodBitInputStream;
import com.vke.core.file.gzip.io.bit.OldGoodBitInputStream;
import com.vke.core.file.gzip.io.bit.ShittyBitInputStream;

import java.io.IOException;
import java.io.InputStream;

public class InflatingDevice {
    private final BitInputStream bitStream;
    private DeflateBlock currentBlock;
    private boolean finished;

    public InflatingDevice(InputStream toBeInflated) {
        this.bitStream = new ShittyBitInputStream(toBeInflated);
    }

    public InflatingDevice(BitInputStream bitStream) {
        this.bitStream = bitStream;
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
                    currentBlock = DeflateBlock.createNextBlock(bitStream);
                }

                int value = currentBlock.nextByte(bitStream);

                if (value != -1) {
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

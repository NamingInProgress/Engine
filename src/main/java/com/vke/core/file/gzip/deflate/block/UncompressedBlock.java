package com.vke.core.file.gzip.deflate.block;

import com.vke.core.file.gzip.deflate.DeflateBlock;
import com.vke.core.file.gzip.io.bit.BitInputStream;
import com.vke.core.file.gzip.io.bit.BitOrdering;

import java.io.IOException;

public class UncompressedBlock implements DeflateBlock {
    private final boolean bFinal;
    private boolean initialized;

    private int length;
    private int bytesRead;

    public UncompressedBlock(boolean bFinal) {
        this.bFinal = bFinal;
    }

    @Override
    public int nextByte(BitInputStream inputStream) throws IOException {
        inputStream.setOrdering(BitOrdering.LSB_FIRST);
        if (!initialized) {
            inputStream.alignToByte();
            length = inputStream.readBits(16);
            int NLEN = inputStream.readBits(16);

            initialized = true;
        }
        try {
            if (bytesRead >= length) return -1;
            int fullByte = inputStream.readBits(8);
            bytesRead++;
            return fullByte;
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public boolean isFinished() {
        return bytesRead >= length;
    }

    @Override
    public boolean bFinal() {
        return bFinal;
    }
}

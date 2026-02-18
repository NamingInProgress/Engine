package com.vke.core.file.deflate;

import com.vke.core.file.deflate.block.DynamicBlock;
import com.vke.core.file.deflate.block.FixedBlock;
import com.vke.core.file.deflate.block.UncompressedBlock;
import com.vke.core.file.deflate.lz77.SlidingWindow;
import com.vke.core.file.io.bit.BitInputStream;
import com.vke.core.file.io.bit.BitOrdering;

import java.io.IOException;

public interface DeflateBlock {
    int TYPE_UNCOMPRESSED = 0;
    int TYPE_FIXED = 1;
    int TYPE_DYNAMIC = 2;

    int nextByte(BitInputStream inputStream) throws IOException;

    boolean isFinished();

    boolean bFinal();

    static DeflateBlock createNextBlock(BitInputStream inputStream, SlidingWindow window) throws IOException {
        inputStream.setOrdering(BitOrdering.LSB_FIRST);
        boolean bFinal = inputStream.readBits(1) == 1;
        int bType = inputStream.readBits(2);

        if (bType == TYPE_UNCOMPRESSED) {
            return new UncompressedBlock(bFinal);
        }

        if (bType == TYPE_FIXED) {
            return new FixedBlock(bFinal, window);
        }

        if (bType == TYPE_DYNAMIC) {
            return new DynamicBlock(bFinal, window);
        }

        throw new IOException("Illegal block header!");
    }
}

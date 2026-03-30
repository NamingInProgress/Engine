package com.vke.core.file.deflate.decompress;

import com.vke.core.file.deflate.decompress.block.DynamicBlock;
import com.vke.core.file.deflate.decompress.block.FixedBlock;
import com.vke.core.file.deflate.decompress.block.UncompressedBlock;
import com.vke.core.file.deflate.decompress.lz77.SlidingWindow;
import com.vke.core.file.io.bit.input.BitInputStream;
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

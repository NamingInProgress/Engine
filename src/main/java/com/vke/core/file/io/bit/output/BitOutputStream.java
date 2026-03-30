package com.vke.core.file.io.bit.output;

import com.vke.core.file.io.bit.BitOrdering;

import java.io.IOException;
import java.io.OutputStream;

public interface BitOutputStream {
    void setOrdering(BitOrdering ordering);
    BitOrdering getOrdering();
    void setPaddingBit(int paddingBit);
    void writeBits(int bits, int amountBits) throws IOException;
    void flushBuffer() throws IOException;
    void alignToByte() throws IOException;

    int partialBits();

    void streamDirectAligned(byte[] data, int start, int length) throws IOException;
}

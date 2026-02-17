package com.vke.core.file.io.bit;

import java.io.IOException;
import java.io.InputStream;

public interface BitInputStream {
    void setOrdering(BitOrdering ordering);
    BitOrdering getOrdering();

    int readBits(int n) throws IOException;
    int peekBits(int n) throws IOException;

    void alignToByte() throws IOException;

    default void appendData(InputStream toAppend) {
        throw new UnsupportedOperationException();
    }
}

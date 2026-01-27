package com.vke.core.file.gzip.io.bit;

import java.io.IOException;

public interface BitInputStream {
    void setOrdering(BitOrdering ordering);
    BitOrdering getOrdering();

    int readBits(int n) throws IOException;
    int peekBits(int n) throws IOException;

    void alignToByte() throws IOException;
}

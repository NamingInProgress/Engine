package com.vke.core.file.gzip.deflate;

import java.io.InputStream;

public class ReturnableBitInputStream {
    private static final int BITS = Integer.BYTES * 8;

    private InputStream stream;
    private int buffer;

    public ReturnableBitInputStream(InputStream stream) {
        this.stream = stream;
    }

    public int readBits(int n) {

    }
}

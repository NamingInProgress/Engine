package com.vke.core.file.utils;

import java.io.IOException;

public abstract class WrappingDecompressor {
    private boolean headerParsed;
    private boolean finished;

    protected abstract void parseHeader() throws IOException;
    protected abstract int decompressBytes(byte[] dst, int start, int length) throws IOException;
    protected abstract void parseFooter() throws IOException;
    protected abstract boolean isFinished();

    public int decompress(byte[] dst, int start, int length) throws IOException {
        if (finished) {
            return -1;
        }

        if (!headerParsed) {
            parseHeader();
            headerParsed = true;
        }

        int read = decompressBytes(dst, start, length);
        if (read == -1 || isFinished()) {
            parseFooter();
            finished = true;
        }
        return read;
    }
}

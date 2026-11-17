package com.vke.utils.io;

import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends InputStream {
    private final InputStream parent;
    private final int limit;
    private int cursor;

    public LimitedInputStream(InputStream parent, int limit) {
        this.parent = parent;
        this.limit = limit;
    }

    @Override
    public int read() throws IOException {
        int next = parent.read();
        cursor++;
        if (cursor > limit) {
            throw new IOException("Tried to read past the limit (" + limit + ") for this input stream!");
        }
        return next;
    }

    @Override
    public int read(byte[] dst, int off, int len) throws IOException {
        int remaining = limit - cursor;
        if (remaining <= 0) {
            return -1;
        }
        int toRead = Math.min(len, remaining);
        int read = parent.read(dst, off, toRead);
        if (read > 0) {
            cursor += read;
        }
        return read;
    }

    public int getLimit() {
        return limit;
    }
}

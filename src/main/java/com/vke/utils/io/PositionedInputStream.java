package com.vke.utils.io;

import java.io.IOException;
import java.io.InputStream;

public class PositionedInputStream extends InputStreamWrapper {
    private long position;

    public PositionedInputStream(InputStream stream) {
        super(stream);
    }

    @Override
    public int read() throws IOException {
        int a = parent.read();
        if (a != -1) {
            position++;
        }
        return a;
    }

    @Override
    public int read(byte[] dst, int off, int len) throws IOException {
        int read = parent.read(dst, off, len);
        if (read > 0) {
            position += read;
        }
        return read;
    }

    public long getPosition() {
        return position;
    }
}

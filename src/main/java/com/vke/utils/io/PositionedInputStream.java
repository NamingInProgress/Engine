package com.vke.utils.io;

import java.io.IOException;
import java.io.InputStream;

public class PositionedInputStream extends InputStream {
    private final InputStream inner;
    private long position;

    public PositionedInputStream(InputStream stream) {
        this.inner = stream;
    }

    @Override
    public int read() throws IOException {
        int a = inner.read();
        if (a != -1) {
            position++;
        }
        return a;
    }

    public long getPosition() {
        return position;
    }
}

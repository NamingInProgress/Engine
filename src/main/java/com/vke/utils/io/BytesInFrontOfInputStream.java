package com.vke.utils.io;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class BytesInFrontOfInputStream extends InputStreamWrapper {
    private final byte[] prefix;
    private int prefixCursor;
    private boolean prefixDone;

    public BytesInFrontOfInputStream(InputStream parent, byte[] prefix) {
        super(parent);
        this.prefix = prefix;
    }

    public BytesInFrontOfInputStream(InputStream parent, byte prefix) {
        super(parent);
        this.prefix = new byte[] { prefix };
    }

    @Override
    public int read() throws IOException {
        if (prefixDone) return parent.read();
        int next = prefix[prefixCursor];
        prefixCursor++;
        if (prefixCursor >= prefix.length) {
            prefixDone = true;
        }
        return next;
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
        if (prefixDone) {
            return parent.read(b, off, len);
        }
        int prefLen = prefix.length;
        int toRead = Math.min(prefLen - prefixCursor, len);

        System.arraycopy(prefix, prefixCursor, b, off, toRead);

        prefixCursor += toRead;
        if (prefixCursor >= prefix.length) {
            prefixDone = true;
        }

        return toRead;
    }
}

package com.vke.utils.io;

// Source - https://stackoverflow.com/a/6603018
// Posted by Kothar, modified by community. See post 'Timeline' for change history
// Retrieved 2026-08-07, License - CC BY-SA 3.0

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferBackedInputStream extends InputStream {

    ByteBuffer buf;

    public ByteBufferBackedInputStream(ByteBuffer buf) {
        this.buf = buf;
    }

    @Override
    public int read() throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }
        return buf.get() & 0xFF;
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        if (!buf.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, buf.remaining());
        buf.get(bytes, off, len);
        return len;
    }
}


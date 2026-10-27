package com.vke.utils.io;

import java.io.IOException;
import java.io.InputStream;

/// Not hyper optimized due to it mainly being used in header parsing
public class BytesInFrontOfInputStream extends InputStream {
    private final InputStream parent;
    private final byte[] prefix;
    private int prefixCursor;
    private boolean prefixDone;

    public BytesInFrontOfInputStream(InputStream parent, byte[] prefix) {
        this.parent = parent;
        this.prefix = prefix;
    }

    public BytesInFrontOfInputStream(InputStream parent, byte prefix) {
        this.parent = parent;
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
}

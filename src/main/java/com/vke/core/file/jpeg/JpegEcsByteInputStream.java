package com.vke.core.file.jpeg;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

public class JpegEcsByteInputStream extends InputStream {
    private final InputStream inner;
    private @Nullable StopReason stopReason;
    private int buffer = -1;

    public JpegEcsByteInputStream(InputStream inner) throws IOException {
        if (!inner.markSupported()) {
            throw new IOException("InputStream MUST support mark");
        }
        this.inner = inner;
        this.buffer = readInner();
    }

    @Override
    public int read() throws IOException {
        int queried = buffer;
        buffer = readInner();
        return queried;
    }

    private int readInner() throws IOException {
        inner.mark(2);
        int next = inner.read();
        if (next == -1) return -1;
        if (next == 0xFF) {
            int stuff = inner.read();
            if (stuff == 0x00) {
                return 0xFF;
            } else {
                inner.reset();
                stopReason = StopReason.EndOfScan;
                return -1;
            }
        }
        return next;
    }

    public @Nullable StopReason getStopReason() {
        return stopReason;
    }

    public enum StopReason {
        EndOfScan,
        Restart
    }
}

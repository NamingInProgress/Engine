package com.vke.utils.io;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamWrapper extends InputStream {
    protected final InputStream parent;

    public InputStreamWrapper(InputStream parent) {
        super();
        this.parent = parent;
    }

    @Override
    public int read() throws IOException {
        return parent.read();
    }

    @Override
    public int read(@NotNull byte[] b) throws IOException {
        return parent.read(b);
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
        return parent.read(b, off, len);
    }

    @Override
    public @NotNull byte[] readAllBytes() throws IOException {
        return parent.readAllBytes();
    }

    @Override
    public @NotNull byte[] readNBytes(int len) throws IOException {
        return parent.readNBytes(len);
    }

    @Override
    public int readNBytes(@NotNull byte[] b, int off, int len) throws IOException {
        return parent.readNBytes(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return parent.skip(n);
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        parent.skipNBytes(n);
    }

    @Override
    public int available() throws IOException {
        return parent.available();
    }

    @Override
    public void close() throws IOException {
        parent.close();
    }

    @Override
    public void mark(int readlimit) {
        parent.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        parent.reset();
    }

    @Override
    public boolean markSupported() {
        return parent.markSupported();
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        return parent.transferTo(out);
    }
}

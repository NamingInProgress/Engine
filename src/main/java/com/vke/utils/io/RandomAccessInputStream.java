package com.vke.utils.io;

import org.jetbrains.annotations.NotNull;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class RandomAccessInputStream extends InputStream {
    private final RandomAccessFile file;

    public RandomAccessInputStream(RandomAccessFile file) {
        this.file = file;
    }

    @Override
    public int read() throws IOException {
        return file.read();
    }

    @Override
    public int read(byte @NotNull [] b) throws IOException {
        return file.read(b);
    }

    @Override
    public int read(byte @NotNull [] b, int off, int len) throws IOException {
        return file.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        long current = file.getFilePointer();
        long length = file.length();
        long target = Math.min(length, current + n);
        file.seek(target);
        return target - current;
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        long skipped = skip(n);
        if (skipped < n) {
            throw new EOFException();
        }
    }

    @Override
    public int available() throws IOException {
        long remaining = file.length() - file.getFilePointer();
        return remaining > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) remaining;
    }

    @Override
    public byte @NotNull [] readNBytes(int len) throws IOException {
        byte[] buffer = new byte[len];
        int read = read(buffer);
        if (read < len) {
            return Arrays.copyOf(buffer, Math.max(read, 0));
        }
        return buffer;
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    public void seek(long ptr) throws IOException {
        file.seek(ptr);
    }

    public long position() throws IOException {
        return file.getFilePointer();
    }
}

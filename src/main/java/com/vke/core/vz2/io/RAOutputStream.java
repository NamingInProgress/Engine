package com.vke.core.vz2.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class RAOutputStream extends OutputStream {
    private final RandomAccessFile file;

    public RAOutputStream(RandomAccessFile file) {
        this.file = file;
    }

    @Override
    public void write(int b) throws IOException {
        file.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        file.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        file.write(b, off, len);
    }

    /**
     * Ts like super expensive, use at your own risk
     * @throws IOException
     */
    @Override
    public void flush() throws IOException {
        file.getFD().sync();
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

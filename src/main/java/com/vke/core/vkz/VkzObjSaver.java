package com.vke.core.vkz;

import com.vke.api.serializer.Saver;
import com.vke.utils.exception.SaveException;

import java.io.*;

public class VkzObjSaver implements Saver, Flushable, Closeable {
    private int partial8;
    private int bitIndex;
    private final OutputStream output;
    private long savedBytes;
    private int savedBits;

    public VkzObjSaver(OutputStream output) {
        this.output = output;
    }

    private void saveBuffer() throws SaveException {
        try {
            output.write(partial8);
            partial8 = 0;
            bitIndex = 0;
        } catch (IOException e) {
            throw new SaveException(e);
        }
    }

    @Override
    public void saveByte(byte v) throws SaveException {
        saveBits(8, v);
    }

    @Override
    public void saveBits(int n, int bitMap) throws SaveException {
        int remaining = 8 - bitIndex;
        if (remaining >= n) {
            partial8 |= (bitMap << (bitIndex));
            bitIndex += n;
        } else {
            int first = rightBits(bitMap, remaining);
            int firstLen = remaining;
            partial8 |= (first << (bitIndex));
            saveBuffer();
            remaining = n - firstLen;
            int second = (bitMap >>> firstLen);
            second = rightBits(second, remaining);
            partial8 |= second;
            bitIndex += remaining;
        }
        savedBits += n;
        savedBytes += Math.floorDiv(savedBits, 8);
        savedBits %= 8;
    }

    public long getSavedBytes() {
        return savedBytes;
    }

    public int getExtraBits() {
        return savedBits;
    }

    private int rightBits(int v, int right) {
        int shift = 32 - right;
        v <<= shift;
        v >>>= shift;
        return v;
    }

    @Override
    public void close() throws IOException {
        output.close();
    }

    @Override
    public void flush() throws IOException {
        try {
            saveBuffer();
        } catch (SaveException e) {
            throw new IOException(e.getCause());
        }
        output.flush();
    }
}

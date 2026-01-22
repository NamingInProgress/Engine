package com.vke.core.vkz;

import com.vke.api.serializer.Loader;
import com.vke.utils.exception.LoadException;

import java.io.IOException;
import java.io.InputStream;

public class VkzObjLoader implements Loader {
    private long buffer64;
    private int bitIndex;
    private int bufLen;
    private final InputStream input;
    private long fullBytes;
    private int extraBits;
    private long bitsRead;

    public VkzObjLoader(InputStream input, long fullBytes, int extraBits) {
        this.input = input;
        this.fullBytes = fullBytes;
        this.extraBits = extraBits;
    }

    private void fillBuffer() throws LoadException {
        try {
            byte[] tryRead = input.readNBytes(8);
            bufLen = tryRead.length * 8;
            buffer64 = 0;
            bitIndex = 0;
            for (int i = 0; i < tryRead.length; i++) {
                int bits8 = tryRead[i] & 0xFF;
                buffer64 |= ((long) bits8 << i * 8);
            }
        } catch (IOException e) {
            throw new LoadException(e);
        }
    }

    @Override
    public byte loadByte() throws LoadException {
        return (byte) loadBits(8);
    }

    @Override
    public int loadBits(int n) throws LoadException {
        long totalBits = (fullBytes << 3) + extraBits;
        if (bitsRead + n > totalBits) {
            throw new LoadException(
                    "Tried to read " + n + " bits from stream, but only " + (totalBits - bitsRead) + " are left!"
            );
        }

        if (bufLen == 0 || bitIndex == bufLen) {
            fillBuffer();
        }

        int remaining = bufLen - bitIndex;
        if (remaining >= n) {
            int value = (int) (buffer64 >>> bitIndex);
            value = rightBits(value, n);
            bitIndex += n;
            bitsRead += n;
            return value;
        } else {
            int first = (int) (buffer64 >>> bitIndex);
            first = rightBits(first, remaining);
            int firstLen = remaining;
            fillBuffer();
            remaining = n - remaining;
            int second = (int) (buffer64 >>> bitIndex);
            second = rightBits(second, remaining);
            second <<= firstLen;
            bitIndex += remaining;
            bitsRead += n;
            return first | second;
        }
    }

    public void setFullBytes(long fullBytes) {
        this.fullBytes = fullBytes;
    }

    public void setExtraBits(int extraBits) {
        this.extraBits = extraBits;
    }

    private int rightBits(int v, int right) {
        int shift = 32 - right;
        v <<= shift;
        v >>>= shift;
        return v;
    }
}

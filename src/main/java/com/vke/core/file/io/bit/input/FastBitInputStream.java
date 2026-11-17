package com.vke.core.file.io.bit.input;

import com.vke.core.file.io.bit.BitOrdering;
import com.vke.utils.IntUtils;
import com.vke.utils.io.LimitedInputStream;

import java.io.IOException;
import java.io.InputStream;

public class FastBitInputStream implements BitInputStream {
    private static final int BUF_BYTES = 4096;

    private BitOrdering ordering;
    private final InputStream input;
    private final long limit;
    private long inputPos;

    private final byte[] buffer = new byte[BUF_BYTES];
    private int bufferPos; //the byte position inside of buffer
    private int bufferLength; //the current length of buffer from 0

    private long bitBuf; //next bits, the actual next one is at front
    private int bitCount; //how many valid bits are in bitBuf (always a multiple of 8 when buffer is first filled)

    public FastBitInputStream(InputStream input, long readLimit) {
        this.input = input;
        this.limit = readLimit;
    }

    public FastBitInputStream(InputStream input) {
        this.input = input;
        this.limit = Long.MAX_VALUE;
    }

    public static FastBitInputStream withLimitRespect(InputStream stream) {
        if (stream instanceof LimitedInputStream limited) {
            return new FastBitInputStream(limited, limited.getLimit());
        } else {
            return new FastBitInputStream(stream);
        }
    }

    @Override
    public void setOrdering(BitOrdering ordering) {
        this.ordering = ordering;
    }

    @Override
    public BitOrdering getOrdering() {
        return ordering;
    }

    private int format(int bits, int n) {
        if (ordering == BitOrdering.LSB_FIRST) {
            return bits;
        } else {
            return Integer.reverse(bits) >>> (32 - n);
        }
    }

    private static long mask(int bits) {
        if (bits >= 64) {
            return -1L;
        }
        return (1L << bits) - 1L;
    }

    private void fillBuffer() throws IOException {
        bufferPos = 0;
        int remaining = IntUtils.longToIntPositive(limit - inputPos);
        int toRead = Math.min(remaining, BUF_BYTES);
        int n = 0;
        while (n < toRead) {
            int read = input.read(buffer, n, toRead - n);
            if (read == -1) {
                break;
            }
            n += read;
        }
        inputPos += n;
        bufferLength = n;
    }

    private void fillBitBuf(int n) throws IOException {
        while (bitCount < n) {
            if (bufferPos >= bufferLength) {
                fillBuffer();
                if (bufferLength == 0) {
                    return;
                }
            }
            bitBuf |= (long) (buffer[bufferPos++] & 0xFF) << bitCount;
            bitCount += 8;
        }
    }

    @Override
    public int readBits(int n) throws IOException {
        fillBitBuf(n);
        int result = (int) (bitBuf & mask(n));
        int used = Math.min(n, bitCount);
        bitBuf >>>= used;
        bitCount -= used;
        return format(result, n);
    }

    @Override
    public int peekBits(int n) throws IOException {
        fillBitBuf(n);
        int result = (int) (bitBuf & mask(n));
        return format(result, n);
    }

    @Override
    public void alignToByte() throws IOException {
        int discard = bitCount & 7;
        bitBuf >>>= discard;
        bitCount -= discard;
    }

    @Override
    public int readBytes(byte[] dst, int start, int length) throws IOException {
        int totalRead = 0;

        while (totalRead < length) {
            fillBitBuf(8);
            if (bitCount < 8) {
                break;
            }
            dst[start + totalRead] = (byte) (bitBuf & 0xFF);
            bitBuf >>>= 8;
            bitCount -= 8;
            totalRead++;
            if (bitCount == 0) {
                break;
            }
        }

        if (bitCount == 0 && totalRead < length) {
            int bufferBytes = Math.min(bufferLength - bufferPos, length - totalRead);
            System.arraycopy(buffer, bufferPos, dst, start + totalRead, bufferBytes);
            bufferPos += bufferBytes;
            totalRead += bufferBytes;
        }

        if (totalRead < length) {
            int allowed = IntUtils.longToIntPositive(limit - inputPos);
            int toRead = Math.min(length - totalRead, allowed);
            if (toRead > 0) {
                int read = input.read(dst, start + totalRead, toRead);
                if (read == -1) {
                    return totalRead;
                }
                inputPos += read;
                totalRead += read;
            }
        }

        return totalRead;
    }
}

package com.vke.core.file.gzip.io.bit;

import com.vke.core.file.gzip.deflate.BitUtils;

import java.io.IOException;
import java.io.InputStream;

public class ShittyBitInputStream implements BitInputStream {
    private final InputStream in;
    private int buffer;
    private int bitsLeft;

    private int peek;
    private int peekLeft;

    private BitOrdering ordering;

    public BitOrdering getOrdering() {
        return ordering;
    }

    public void setOrdering(BitOrdering ordering) {
        this.ordering = ordering;
    }

    public ShittyBitInputStream(InputStream in) {
        this.in = in;
        this.buffer = 0;
        this.bitsLeft = 0;
    }

    public int readBit() throws IOException {
        if (peekLeft > 0) {
            int res = peek & 1;
            peek >>>= 1;
            peekLeft--;
            return res;
        }

        if (bitsLeft == 0) {
            int nextByte = in.read();
            if (nextByte == -1) {
                return -1;
            }
            buffer = nextByte;
            bitsLeft = 8;
        }

        int bit = buffer & 1;
        buffer >>>= 1;
        bitsLeft--;

        return bit;
    }

    public int readBits0(int n) throws IOException {
        if (n < 0 || n > 32) {
            throw new IllegalArgumentException("n must be between 0 and 32");
        }

        int result = 0;
        int collected = 0;

        while (collected < n) {
            int bit = readBit();
            if (bit == -1) {
                return collected == 0 ? -1 : result;
            }

            result |= bit << collected;
            collected++;
        }

        return result;
    }

    @Override
    public int readBits(int n) throws IOException {
        if (n == 0) return 0;
        return formatBits(readBits0(n), n);
    }

    private int formatBits(int asLsb, int n) {
        if (ordering == BitOrdering.LSB_FIRST) {
            return asLsb;
        } else {
            return Integer.reverse(asLsb) >>> (32 - n);
        }
    }

    @Override
    public void alignToByte() throws IOException {
        int bitsToSkip = (8 - (bitsLeft % 8)) % 8;
        readBits0(bitsToSkip);
    }

    @Override
    public int peekBits(int n) throws IOException {
        if (peekLeft < n) {
            int total = peekLeft;
            int shift = peekLeft;
            while (total < n) {
                int pl = peekLeft;
                peekLeft = 0;
                peek |= (readBit() << shift);
                peekLeft = pl;
                shift++;
                total++;
                peekLeft++;
            }
            return peekBits(n);
        } else {
            return formatBits(BitUtils.right(peek, n), n);
        }
    }

    public void close() throws IOException {
        in.close();
    }
}

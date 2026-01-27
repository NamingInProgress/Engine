package com.vke.core.file.gzip.io.bit;

import com.vke.core.file.gzip.deflate.BitUtils;
import com.vke.utils.Utils;

import java.io.IOException;
import java.io.InputStream;

public class OldGoodBitInputStream implements BitInputStream {
    private static final int BITS = 32;

    private final InputStream input;
    private BitOrdering ordering;

    private int buffer;
    private int bitCursor;
    private boolean inputStreamDone;
    private int availableBits;

    private int peekBuf;
    private int peekBufLen;

    public OldGoodBitInputStream(InputStream input, BitOrdering ordering) {
        this.input = input;
        this.ordering = ordering;
        this.bitCursor = BITS;
    }

    public OldGoodBitInputStream(InputStream input) {
        this(input, BitOrdering.LSB_FIRST);
    }

    /**
     * Returns the next {@code n} bits, incrementing the cursor by {@code n}.
     * @param n The amount of bits to read. Allowed range is {@code 0 <= n <= 32}
     * @return The resulting bits represented as an {@code int}. The ordering of bits follows the configured {@link BitOrdering}.
     * @throws IOException If the underlying {@link InputStream} throws an Exception
     */
    @Override
    public int readBits(int n) throws IOException {
        if (n == 0) return 0;
        return formatBits(readBits0(n), n);
    }

    //i am actually very proud since i didnt use chatgpt at all here
    private int readBits0(int n) throws IOException {
        //first check if the peek buffer has contents inside
        if (peekBufLen > 0) {
            if (peekBufLen >= n) {
                //path 1: the peek buffer is already big enough
                //strat: extract the lower n bits and shift the remaining bits in the peek buffer over
                int lowerLsb = BitUtils.truncateBits(peekBuf, BITS - n, 0);
                peekBuf >>>= n;
                peekBufLen -= n;
                return lowerLsb;
            } else {
                //path 2: the peek buffer only covers a fraction of the required data
                //strat: completely clear the peek buffer and set length to 0
                //       then get the missing top bits from a call to readBits()
                int missing = n - peekBufLen;
                int lowerLsb = peekBuf;
                int lowerLen = peekBufLen;
                peekBuf = 0;
                peekBufLen = 0;
                int topLsb = readBits0(missing) << lowerLen;
                return lowerLsb | topLsb;
            }
        }
        checkInputBuffer();

        checkDataSufficient(n);

        //remember: java int MSB........LSB
        //cursor starts at the RIGHT
        int remainingBits = BITS - bitCursor;

        int asLsb = 0;
        if (n <= remainingBits) {
            //path 1: buffer still has enough bits left
            //strat: extract the remaining top bits
            int left = BITS - (bitCursor + n);
            asLsb = BitUtils.truncateBits(buffer, left, bitCursor) >>> bitCursor;
            bitCursor += n;
        } else {
            //path 2: buffer has some of the data, but the other bits have to be resolved
            //strat: extract all the remaining bits and shift them over, then fill the buffer again and exztract
            //remaining bits.
            int available = BITS - bitCursor;
            int takeLower = Math.min(n, available);
            int lower = (buffer >>> bitCursor) & ((1 << takeLower) - 1);

            bitCursor += takeLower;
            if (takeLower == n) {
                asLsb = lower;
            } else {
                int remaining = n - takeLower;
                checkInputBuffer();
                int upper = buffer & ((1 << remaining) - 1);
                bitCursor = remaining;
                asLsb = lower | (upper << takeLower);
            }
        }

        return asLsb;
    }

    /**
     * Returns the next {@code n} bits, but the cursor does not advance. This means,
     * that multiple calls to {@code peekBits} where n stays consistent will return
     * the same result.
     * @param n The amount of bits to read. Allowed range is {@code 0 <= n <= 32}
     * @return The resulting bits represented as an {@code int}. The ordering of bits follows the configured {@link BitOrdering}.
     * @throws IOException If the underlying {@link InputStream} throws an Exception
     */
    @Override
    public int peekBits(int n) throws IOException {
        if (n == 0) return 0;
        if (peekBufLen < n) {
            //path 1: peek buffer is not sufficient enough
            //strat: call readBits() with the missing amount and insert as top bits into peek buffer
            int missing = n - peekBufLen;
            int oldLen = peekBufLen;
            //readBits would break otherwise
            peekBufLen = 0;
            checkInputBuffer();
            int totalRemainingBits = assumeRemainingBits();
            if (isSourceEmpty() && totalRemainingBits < missing) {
                int canRead = missing - totalRemainingBits;
                int topBits = readBits0(canRead);
                peekBufLen = oldLen;
                topBits <<= peekBufLen;
                peekBuf |= topBits;
                peekBufLen += missing;
            } else {
                int topBits = readBits0(missing);
                peekBufLen = oldLen;
                topBits <<= peekBufLen;
                peekBuf |= topBits;
                peekBufLen += missing;
            }
        }
        int asLsb = BitUtils.truncateBits(peekBuf, BITS - (peekBufLen - n), 0);
        return formatBits(asLsb, n);
    }

    /**
     * Aligns the cursor to the next byte boundary and skips over the in-between bits.
     * @throws IOException
     */
    @Override
    public void alignToByte() throws IOException {
        int bitsToSkip = (8 - (bitCursor % 8)) % 8;
        readBits0(bitsToSkip);
    }

    @Override
    public void setOrdering(BitOrdering ordering) {
        this.ordering = ordering;
    }

    @Override
    public BitOrdering getOrdering() {
        return ordering;
    }

    public boolean isSourceEmpty() {
        return inputStreamDone;
    }

    public int assumeRemainingBits() {
        return availableBits + peekBufLen - bitCursor;
    }

    private int formatBits(int asLsb, int n) {
        if (ordering == BitOrdering.LSB_FIRST) {
            return asLsb;
        } else {
            return Integer.reverse(asLsb) >>> (32 - n);
        }
    }

    private void checkInputBuffer() throws IOException {
        if (bitCursor >= BITS - 1) {
            byte[] b = input.readNBytes(4);
            availableBits = b.length * 8;
            if (b.length != 4) {
                fillBufferFromBytes(b);
                inputStreamDone = true;
                bitCursor = 0;
            } else {
                fillBufferFromBytes(b);
                bitCursor = 0;
            }
        }
    }

    private void fillBufferFromBytes(byte[] bytes) {
        int a = 0;
        for (int i = 0; i < bytes.length; i++) {
            int b = Utils.unsignByte(bytes[i]);
            int shift = i * 8;
            a |= (b << shift);
        }
        buffer = a;
    }

    private void checkDataSufficient(int required) throws IOException {
        int left = assumeRemainingBits();
        if (inputStreamDone && left < required) {
            throw new IOException("Tried to read " + required + " bits from stream, but only " + left + " are left!");
        }
    }
}

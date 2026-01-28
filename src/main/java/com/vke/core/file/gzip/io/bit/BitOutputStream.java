package com.vke.core.file.gzip.io.bit;

import com.vke.core.file.gzip.deflate.BitUtils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * will be LSB-first!
 * Class to write single bits to an {@link OutputStream}. The resulting data
 * @author Julian Hohenhausen
 */
public class BitOutputStream {
    private static final int BITS = 32;

    private final OutputStream output;
    private final BitOrdering ordering;
    private int padding;

    private int bitAccumulator;
    private int accumulatorCursor;

    public BitOutputStream(OutputStream output, BitOrdering ordering) {
        this.output = output;
        this.ordering = ordering;
        this.padding = 0;
    }

    public BitOutputStream(OutputStream output) {
        this(output, BitOrdering.LSB_FIRST);
    }

    /**
     * Sets the padding bit. Only the LSB matters.
     */
    public void pad(int bit) {
        padding = bit & 1;
    }

    /**
     * Write {@code bitLength} bits. If this operation actually calls {@link OutputStream#write(int)},
     * is not defined. The bits might get accumulated first.
     * @param bits the bits to write. They are expected in the order specified by the provided {@link BitOrdering}
     * @param bitLength the amount of bits to write
     * @throws IOException if {@link OutputStream#write(int)} throws an error
     */
    public void writeBits(int bits, int bitLength) throws IOException {
        int lsbBits = unformatBits(bits);

        if (accumulatorCursor + bitLength > BITS) {
            //path 1: the accumulator will overflow
            //strat: extract the lower bits and fill the accumulator, then flush it.
            //       afterwards fill the accumulator with the remaining bits
            int space = BITS - accumulatorCursor;
            int remaining = bitLength - space;
            int lowerLsb = BitUtils.truncateBits(lsbBits, BITS - space, 0);
            lowerLsb <<= accumulatorCursor;
            bitAccumulator |= lowerLsb;
            writeInt(bitAccumulator);
            bitAccumulator = 0;
            accumulatorCursor = remaining;
            if (remaining != 0) {
                int upperLsb = BitUtils.truncateBits(lsbBits, 0, space);
                upperLsb >>>= space;
                bitAccumulator |= upperLsb;
            }
        } else {
            //path 2: the requested bits fit into the accumulator
            //strat: copy the bits into accumulator
            //this line is just a safety messure
            lsbBits = BitUtils.truncateBits(lsbBits, BITS - bitLength, 0);
            int insert = lsbBits << accumulatorCursor;
            bitAccumulator |= insert;
            accumulatorCursor += bitLength;
        }
    }

    /**
     * Flushes all buffered bits to the {@link OutputStream} and pads the remaining space with
     * the padding set in {@link BitOutputStream#pad(int)}. This method also calls {@link OutputStream#flush()}.
     */
    public void flush() throws IOException {
        int bytesToWrite = Math.ceilDiv(accumulatorCursor, 8);
        int toWrite = bitAccumulator;
        if (padding != 0) {
            for (int i = accumulatorCursor; i < BITS; i++) {
                toWrite |= (padding << i);
            }
        }
        for (int i = 0; i < bytesToWrite; i++) {
            int shift = i * 8;
            int currentByte = toWrite >> shift;
            output.write(currentByte);
        }
        output.flush();
    }

    private void writeInt(int i) throws IOException {
        int i1 = i & 0xFF;
        int i2 = (i >> 8) & 0xFF;
        int i3 = (i >> 16) & 0xFF;
        int i4 = (i >> 24) & 0xFF;
        output.write(i1);
        output.write(i2);
        output.write(i3);
        output.write(i4);
    }

    private int unformatBits(int bits) {
        if (ordering == BitOrdering.LSB_FIRST) {
            return bits;
        } else {
            return Integer.reverse(bits);
        }
    }
}

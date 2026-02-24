package com.vke.core.file.io.bit.output;

import com.vke.core.file.io.bit.BitOrdering;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.OutputStream;

public class GoodBitOutputStream implements BitOutputStream {
    private final OutputStream outputStream;

    private BitOrdering ordering;
    private int paddingBit;

    private int buffer;
    private int cursor;

    public GoodBitOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public BitOrdering getOrdering() {
        return ordering;
    }

    @Override
    public void setOrdering(BitOrdering ordering) {
        this.ordering = ordering;
    }

    @Override
    public void setPaddingBit(int paddingBit) {
        this.paddingBit = paddingBit & 1;
    }

    @Override
    public void writeBits(int bits, int amountBits) throws IOException {
        if (ordering == BitOrdering.MSB_FIRST) {
            bits = Integer.reverse(bits) >>> (32 - amountBits);
        }

        bits &= (1 << amountBits) - 1;

        buffer |= bits << cursor;
        cursor += amountBits;

        while (cursor >= 8) {
            outputStream.write(buffer & 0xFF);
            buffer >>>= 8;
            cursor -= 8;
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        while (cursor >= 8) {
            outputStream.write(buffer & 0xFF);
            buffer >>>= 8;
            cursor -= 8;
        }

        if (cursor > 0) {
            outputStream.write(buffer & 0xFF); // remaining bits (already zero padded)
        }

        outputStream.flush();
        buffer = 0;
        cursor = 0;
    }

    @Override
    public void alignToByte() throws IOException {
        int bitsToSkip = (8 - (cursor % 8)) % 8;
        if (bitsToSkip > 0) {
            writeBits(0, bitsToSkip);
        }
    }

    @Override
    public int partialBits() {
        return cursor;
    }

    @Override
    public void streamDirectAligned(byte[] data, int start, int length) throws IOException {
        if (cursor % 8 != 0) throw new IOException("streamDirect can only be used when the bitstream is aligned to bytes!");
        if (cursor > 0) {
            if (cursor == 8) {
                outputStream.write(buffer & 0xFF);
            } else if (cursor == 16) {
                outputStream.write(buffer & 0xFF);
                buffer >>>= 8;
                outputStream.write(buffer & 0xFF);
            }  else if (cursor == 24) {
                outputStream.write(buffer & 0xFF);
                buffer >>>= 8;
                outputStream.write(buffer & 0xFF);
                buffer >>>= 8;
                outputStream.write(buffer & 0xFF);
            }  else if (cursor == 32) {
                outputStream.write(buffer & 0xFF);
                buffer >>>= 8;
                outputStream.write(buffer & 0xFF);
                buffer >>>= 8;
                outputStream.write(buffer & 0xFF);
                buffer >>>= 8;
                outputStream.write(buffer & 0xFF);
            }
            cursor = 0;
        }
        outputStream.write(data, start, length);
    }

    private int unformatBits(int bits, int n) {
        if (ordering == BitOrdering.LSB_FIRST) {
            return bits;
        } else {
            return Integer.reverse(bits) >>> (32 - n);
        }
    }

    private void writeInt(int i) throws IOException {
        DataUtils.writeU32LittleEndian(outputStream, i);
    }
}

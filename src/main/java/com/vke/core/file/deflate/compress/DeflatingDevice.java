package com.vke.core.file.deflate.compress;

import com.vke.core.file.deflate.decompress.InflatingDevice;
import com.vke.core.file.io.bit.BitOrdering;
import com.vke.core.file.io.bit.output.BitOutputStream;
import com.vke.core.file.io.bit.output.GoodBitOutputStream;

import java.io.IOException;
import java.io.OutputStream;

public class DeflatingDevice {
    private static final int SIZE_THRESHOLD = InflatingDevice.SLIDING_WINDOW_SIZE * 2 - 1;

    private final BitOutputStream bitOutputStream;
    private final BlockBuilder blockBuilder;

    public DeflatingDevice(OutputStream outputStream, int maxChainChecks) {
        this.bitOutputStream = new GoodBitOutputStream(outputStream);
        this.bitOutputStream.setOrdering(BitOrdering.LSB_FIRST);
        this.blockBuilder = new BlockBuilder(SIZE_THRESHOLD, maxChainChecks, InflatingDevice.SLIDING_WINDOW_SIZE);
    }

    public void deflateNext(byte b) throws IOException {
        deflateNext(new byte[] {b});
    }

    public void deflateNext(byte[] bytes) throws IOException {
        deflateNext(bytes, 0, bytes.length);
    }

    public void deflateNext(byte[] bytes, int start, int length) throws IOException {
        blockBuilder.onNextBytes(bytes, start, length, bitOutputStream);
    }

    public void finish() throws IOException {
        blockBuilder.flushBlock(bitOutputStream, true);
        bitOutputStream.flushBuffer();
    }
}

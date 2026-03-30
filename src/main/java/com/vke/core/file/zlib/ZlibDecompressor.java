package com.vke.core.file.zlib;

import com.vke.core.file.deflate.decompress.InflatingDevice;
import com.vke.core.file.deflate.decompress.check.Adler;
import com.vke.core.file.deflate.exc.InflatingException;
import com.vke.core.file.io.bit.input.BitInputStream;
import com.vke.core.file.io.bit.BitOrdering;
import com.vke.core.file.io.bit.BitStreamUtils;
import com.vke.core.file.utils.HBFDecodeSource;

import java.io.IOException;
import java.io.InputStream;

public class ZlibDecompressor implements HBFDecodeSource<Integer> {
    private final BitInputStream stream;
    private InflatingDevice device;
    private int cm;
    private int flags;

    private final Adler adler;

    public ZlibDecompressor(BitInputStream stream) {
        this.stream = stream;
        this.adler = new Adler();
    }

    @Override
    public void parseHeader() throws IOException {
        stream.setOrdering(BitOrdering.LSB_FIRST);
        int cmf = stream.readBits(8);
        int flg = stream.readBits(8);

        int cm = cmf & 0x0F;
        int cinfo = (cmf >>> 4) & 0x0F;
        int fcheck = flg & 0x1F;
        int fdict = (flg >>> 5) & 0x01;
        int flevel = (flg >>> 6) & 0x03;

        int header = (cmf << 8) | flg;
        if (header % 31 != 0) {
            throw new IOException("Corrupted ZLib header: FCHECK failed");
        }

        if (cm != 8) {
            throw new IOException("Unsupported compression method: " + cm);
        }

        if (fdict != 0) {
            throw new IOException("Preset dictionary not supported");
        }

        if (cinfo > 7) {
            throw new IOException("Invalid zlib window size");
        }
        int windowSize = 1 << (cinfo + 8);

        device = new InflatingDevice(adler, stream, windowSize);
    }

    public void appendData(InputStream toAppend) {
        stream.appendData(toAppend);
    }

    @Override
    public Integer nextByte() throws IOException {
        if (device.isFinished()) return -1;
        try {
            return device.inflateNextByte();
        } catch (InflatingException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void parseFooter() throws IOException {
        stream.setOrdering(BitOrdering.LSB_FIRST);
        stream.alignToByte();
        int adler = BitStreamUtils.readBigEndian32(stream);
        int computedAdler = this.adler.get();
        if (adler != computedAdler) {
            throw new IOException(String.format("Adler32 mismatch: expected=%08d actual=%08d", adler, computedAdler));
        }
    }
}

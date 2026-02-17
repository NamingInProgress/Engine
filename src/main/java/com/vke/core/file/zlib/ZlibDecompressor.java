package com.vke.core.file.zlib;

import com.vke.core.file.deflate.InflatingDevice;
import com.vke.core.file.deflate.check.Adler;
import com.vke.core.file.deflate.exc.InflatingException;
import com.vke.core.file.io.bit.BitInputStream;
import com.vke.core.file.io.bit.BitOrdering;
import com.vke.core.file.io.bit.BitStreamUtils;
import com.vke.core.file.utils.HBFDecodeSource;

import java.io.IOException;

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
        cm = stream.readBits(4);
        flags = stream.readBits(4);

        int cinfo = stream.readBits(8);

        if (cinfo > 7) {
            throw new IOException("Invalid zlib window size");
        }

        int windowSize = 1 << (cinfo + 8);
        device = new InflatingDevice(adler, stream, windowSize);

        int fcheck = stream.readBits(5);
        int fdict = stream.readBits(1);
        int flevel = stream.readBits(2);

        int cmf = (cinfo << 4) | cm;
        int flg = (flevel << 6) | (fdict << 5) | fcheck;

        int header = (cmf << 8) | flg;
        if (header % 31 != 0) {
            throw new IOException("Corrupted ZLib file: FCHECK failed");
        }

        //only deflate allowed
        if (cm != 8) {
            throw new IOException("Unsupported compression method: " + cm);
        }

        //i dont wanna do dics and noone ever uses this appearently so im fine
        if (fdict != 0) {
            throw new IOException("Preset dictionary not supported");
        }
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
        int adler = BitStreamUtils.readBigEndian32(stream);
        int computedAdler = this.adler.get();
        if (adler != computedAdler) {
            throw new IOException(String.format("Adler32 mismatch: expected=%08x actual=%08x", adler, computedAdler));
        }
    }
}

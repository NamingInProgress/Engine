package com.vke.core.file.ogg.vorbis.header;

import com.vke.core.file.io.bit.BitOrdering;
import com.vke.core.file.io.bit.BitStreamUtils;
import com.vke.core.file.io.bit.input.BitInputStream;
import com.vke.core.file.ogg.vorbis.VorbisStreamUndecodableException;

import java.io.IOException;

public class IdentHeader {
    public final long vorbisVersion;
    public final int channels;
    public final long sampleRate;
    public final long bitrateMaximum;
    public final long bitrateNominal;
    public final long bitrateMinimum;
    public final long blockSize0;
    public final long blockSize1;
    public final boolean framingFlag;

    public IdentHeader(BitInputStream bitStream) throws IOException {
        bitStream.setOrdering(BitOrdering.LSB_FIRST);
        this.vorbisVersion = BitStreamUtils.readLittleEndian32(bitStream);
        this.channels = BitStreamUtils.read8(bitStream);
        this.sampleRate = BitStreamUtils.readLittleEndian32(bitStream);
        this.bitrateMaximum = BitStreamUtils.readLittleEndian32(bitStream);
        this.bitrateNominal = BitStreamUtils.readLittleEndian32(bitStream);
        this.bitrateMinimum = BitStreamUtils.readLittleEndian32(bitStream);
        this.blockSize0 = 1L << (long) bitStream.readBits(4);
        this.blockSize1 = 1L << (long) bitStream.readBits(4);
        this.framingFlag = BitStreamUtils.readFlag(bitStream);

        if (vorbisVersion != 0) throw new VorbisStreamUndecodableException();
        if (!framingFlag) throw new VorbisStreamUndecodableException();
        if (blockSize0 > blockSize1) throw new VorbisStreamUndecodableException();

        bitStream.alignToByte();
    }

    @Override
    public String toString() {
        return "IdentHeader{" +
                "vorbisVersion=" + vorbisVersion +
                ", channels=" + channels +
                ", sampleRate=" + sampleRate +
                ", bitrateMaximum=" + bitrateMaximum +
                ", bitrateNominal=" + bitrateNominal +
                ", bitrateMinimum=" + bitrateMinimum +
                ", blockSize0=" + blockSize0 +
                ", blockSize1=" + blockSize1 +
                ", framingFlag=" + framingFlag +
                '}';
    }
}

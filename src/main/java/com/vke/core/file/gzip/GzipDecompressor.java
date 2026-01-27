package com.vke.core.file.gzip;

import com.vke.core.file.gzip.deflate.BitUtils;
import com.vke.core.file.gzip.deflate.InflatingDevice;
import com.vke.core.file.gzip.deflate.exc.InflatingException;
import com.vke.core.file.gzip.io.bit.BitInputStream;
import com.vke.core.file.gzip.io.bit.BitOrdering;
import com.vke.core.file.gzip.io.bit.BitStreamUtils;

import java.io.IOException;

public class GzipDecompressor {
    private static final int FTEXT    = 0x01;
    private static final int FHCRC    = 0x02;
    private static final int FEXTRA   = 0x04;
    private static final int FNAME    = 0x08;
    private static final int FCOMMENT = 0x10;

    private BitInputStream stream;
    private InflatingDevice inflatingDevice;

    private CompressionMethod compressionMethod;
    private int flags;
    private int lastModified;
    private int os;

    private long crc32;
    private long isize;

    public GzipDecompressor(BitInputStream stream) {
        this.stream = stream;
        this.inflatingDevice = new InflatingDevice(stream);
    }

    public void parseHeader() throws IOException {
        stream.setOrdering(BitOrdering.LSB_FIRST);
        int id1 = stream.readBits(8);
        int id2 = stream.readBits(8);

        if (id1 != 0x1f || id2 != 0x8b) {
            throw new IOException("Corrupted Gzip file.");
        }

        int cm = stream.readBits(8);
        if (cm < 0 || cm > 8) {
            throw new IOException("Illegal compression method used (" + cm + "). Expected value between 0 and =8");
        }
        compressionMethod = CompressionMethod.values()[cm];

        flags = stream.readBits(8);

        lastModified = stream.readBits(32);
        int compressorFlags = stream.readBits(8);
        os = stream.readBits(8);

        if (BitUtils.bitsContains(flags, FEXTRA)) {
            int xlen = BitStreamUtils.readLittleEndian16(stream);
            for (int i = 0; i < xlen; i++) {
                stream.readBits(8);
            }
        }

        if (BitUtils.bitsContains(flags, FNAME)) {
            String name = BitStreamUtils.readNullTermStr(stream, -1);
        }

        if (BitUtils.bitsContains(flags, FCOMMENT)) {
            String comment = BitStreamUtils.readNullTermStr(stream, -1);
        }

        if (BitUtils.bitsContains(flags, FHCRC)) {
            stream.readBits(8);
            stream.readBits(8);
        }
    }

    public void parseFooter() throws IOException {
        stream.setOrdering(BitOrdering.LSB_FIRST);
        crc32 = BitStreamUtils.readLittleEndian32(stream) & 0xffffffffL;
        isize = BitStreamUtils.readLittleEndian32(stream) & 0xffffffffL;
        //i know that i would have to validate the data but cmon i cant be asked bruh
    }


    public int nextByte() throws IOException {
        if (compressionMethod != CompressionMethod.DEFLATE) {
            throw new IOException("Illegal compression method used! Only Deflate can be used with gzip!");
        }
        if (inflatingDevice.isFinished()) {
            return -1;
        }
        try {
            return inflatingDevice.inflateNextByte();
        } catch (InflatingException e) {
            throw new IOException(e);
        }
    }

    private enum CompressionMethod {
        RESERVED0,
        RESERVED1,
        RESERVED2,
        RESERVED3,
        RESERVED4,
        RESERVED5,
        RESERVED6,
        RESERVED7,
        DEFLATE
    }
}

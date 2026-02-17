package com.vke.core.file.gzip;

import com.vke.core.file.deflate.BitUtils;
import com.vke.core.file.deflate.InflatingDevice;
import com.vke.core.file.deflate.check.Crc32;
import com.vke.core.file.deflate.exc.InflatingException;
import com.vke.core.file.io.bit.BitInputStream;
import com.vke.core.file.io.bit.BitOrdering;
import com.vke.core.file.io.bit.BitStreamUtils;
import com.vke.core.file.utils.HBFDecodeSource;

import java.io.IOException;

public class GzipDecompressor implements HBFDecodeSource<Integer> {
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

    private Crc32 crc32;

    public GzipDecompressor(BitInputStream stream) {
        this.stream = stream;
        this.crc32 = new Crc32();
        this.inflatingDevice = new InflatingDevice(crc32, stream);
    }

    @Override
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

    @Override
    public void parseFooter() throws IOException {
        stream.setOrdering(BitOrdering.LSB_FIRST);
        long crc32 = BitStreamUtils.readLittleEndian32(stream) & 0xffffffffL;
        long isize = BitStreamUtils.readLittleEndian32(stream) & 0xffffffffL;

        int deflateCrc32 = this.crc32.get();
        long computedCrc32 = deflateCrc32 & 0xffffffffL;

        if (computedCrc32 != crc32) {
            throw new IOException(String.format("CRC32 mismatch: expected=%08x actual=%08x", crc32, computedCrc32));
        }
    }


    @Override
    public Integer nextByte() throws IOException {
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

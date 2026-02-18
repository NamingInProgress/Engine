package com.vke.core.file.png.chunks;

import com.vke.core.file.deflate.check.Crc32;
import com.vke.core.file.io.bit.BitInputStream;
import com.vke.core.file.io.bit.BitOrdering;
import com.vke.core.file.io.bit.BitStreamUtils;
import com.vke.core.file.io.bit.ShittyBitInputStream;
import com.vke.core.file.png.PngInfo;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public abstract class PngChunk {
    protected int dataLength;
    protected int chunkType;
    protected int crc;
    protected Crc32 checksum;

    public static PngChunk readNextChunk(InputStream input) throws IOException {
        int dataLength = DataUtils.readU32BigEndian(input);
        int type = DataUtils.readU32BigEndian(input);
        PngChunk chunk = switch (type) {
            case 0x49454E44 -> new IEND();
            case 0x49484452 -> new IHDR();
            case 0x504C5445 -> new PLTE();
            case 0x49444154 -> new IDAT();
            case 0x74524E53 -> new tRNS();
            default -> new UnknownChunk();
        };

        chunk.dataLength = dataLength;
        chunk.chunkType = type;

        chunk.checksum = new Crc32();
        chunk.checksum.nextByte((type >>> 24) & 0xFF);
        chunk.checksum.nextByte((type >>> 16) & 0xFF);
        chunk.checksum.nextByte((type >>> 8) & 0xFF);
        chunk.checksum.nextByte(type & 0xFF);

        chunk.readContents(input);

        chunk.crc = DataUtils.readU32BigEndian(input);
        if (chunk.checksum.get() != chunk.crc) {
            throw new IOException("CRC mismatch in chunk " + chunk.getChunkTypeString());
        }

        return chunk;
    }

    protected abstract void readContents(InputStream stream) throws IOException;

    protected int readInt32(InputStream stream) throws IOException {
        int a = DataUtils.readU8(stream);
        int b = DataUtils.readU8(stream);
        int c = DataUtils.readU8(stream);
        int d = DataUtils.readU8(stream);
        checksum.nextByte(a);
        checksum.nextByte(b);
        checksum.nextByte(c);
        checksum.nextByte(d);
        return DataUtils.abcd(a, b, c, d);
    }

    protected int readInt8(InputStream stream) throws IOException {
        int b = DataUtils.readU8(stream);
        checksum.nextByte(b);
        return b;
    }

    public String getChunkTypeString() {
        return new String(new byte[]{
                (byte) ((chunkType >>> 24) & 0xFF),
                (byte) ((chunkType >>> 16) & 0xFF),
                (byte) ((chunkType >>> 8) & 0xFF),
                (byte) (chunkType & 0xFF)
        }, StandardCharsets.US_ASCII);
    }
}
package com.vke.core.file.gzip.io.bit;

import com.carrotsearch.hppc.ByteArrayList;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BitStreamUtils {
    public static int readLittleEndian16(BitInputStream stream) throws IOException {
        int lo = stream.readBits(8);
        int hi = stream.readBits(8);
        return lo | (hi << 8);
    }

    public static int readLittleEndian32(BitInputStream stream) throws IOException {
        int b0 = stream.readBits(8);
        int b1 = stream.readBits(8);
        int b2 = stream.readBits(8);
        int b3 = stream.readBits(8);
        return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
    }

    public static String readNullTermStr(BitInputStream stream, int sizeHint) throws IOException {
        return readNullTermStr(stream, sizeHint, StandardCharsets.UTF_8);
    }

    public static String readNullTermStr(BitInputStream stream, int sizeHint, Charset charset) throws IOException {
        ByteArrayList bytes = sizeHint < 0 ? new ByteArrayList() : new ByteArrayList(sizeHint);
        while (true) {
            byte b = (byte) stream.readBits(8);
            if (b == 0x00) {
                return new String(bytes.toArray(), charset);
            }
            bytes.add(b);
        }
    }
}

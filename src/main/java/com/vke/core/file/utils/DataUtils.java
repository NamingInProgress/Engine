package com.vke.core.file.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class DataUtils {
    public static int abcd(int a, int b, int c, int d) {
        return (a << 24) | (b << 16) | (c << 8) | d;
    }

    public static int dcba(int a, int b, int c, int d) {
        return (d << 24) | (c << 16) | (b << 8) | a;
    }

    public static int readU8(InputStream stream) throws IOException {
        int i = stream.read();
        return i == -1 ? -1 : i & 0xFF;
    }

    public static int readU16LittleEndian(InputStream stream) throws IOException {
        int a = stream.read();
        int b = stream.read();
        if (a == -1 || b == -1) return -1;
        return abcd(0, 0, b, a);
    }

    public static int readU32LittleEndian(InputStream stream) throws IOException {
        int a = stream.read();
        int b = stream.read();
        int c = stream.read();
        int d = stream.read();
        if (a == -1 || b == -1 || c == -1 || d == -1) return -1;
        return abcd(d, c, b, a);
    }

    public static int readU16BigEndian(InputStream stream) throws IOException {
        int a = stream.read();
        int b = stream.read();
        if (a == -1 || b == -1) return -1;
        return abcd(0, 0, a, b);
    }

    public static int readU32BigEndian(InputStream stream) throws IOException {
        int a = stream.read();
        int b = stream.read();
        int c = stream.read();
        int d = stream.read();
        if (a == -1 || b == -1 || c == -1 || d == -1) return -1;
        return dcba(d, c, b, a);
    }

    public static long unsignInt(int size) {
        return Integer.toUnsignedLong(size);
    }

    public static UUID readGuidLittleEndian(InputStream stream) throws IOException {
        long data1 = DataUtils.readU32LittleEndian(stream);
        int data2 = DataUtils.readU16LittleEndian(stream);
        int data3 = DataUtils.readU16LittleEndian(stream);

        byte[] data4 = stream.readNBytes(8);
        if (data4.length != 8) {
            throw new EOFException("Incomplete GUID");
        }

        long msb = (data1 << 32)
                | ((long) data2 << 16)
                | (long) data3;

        long lsb = 0;
        for (int i = 0; i < 8; i++) {
            lsb = (lsb << 8) | (data4[i] & 0xFF);
        }

        return new UUID(msb, lsb);
    }
}

package com.vke.core.file.utils;

import java.io.IOException;
import java.io.InputStream;

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
}

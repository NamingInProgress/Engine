package com.vke.core.file.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Ascii4 {
    private final byte a, b, c, d;
    private final int combined;

    public Ascii4(byte a, byte b, byte c, byte d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.combined = (a << 24) | (b << 16) | (c << 8) | d;
    }

    public Ascii4(char a, char b, char c, char d) {
        this.a = (byte) a;
        this.b = (byte) b;
        this.c = (byte) c;
        this.d = (byte) d;
        this.combined = (a << 24) | (b << 16) | (c << 8) | d;
    }

    public int toInt() {
        return combined;
    }

    public static Ascii4 of(CharSequence sequence) {
        if (sequence.length() != 4) throw new RuntimeException("Illegal length found for Ascii4 literal -> only 4 is allowed obv you idiot!");
        return new Ascii4(
                sequence.charAt(0),
                sequence.charAt(1),
                sequence.charAt(2),
                sequence.charAt(3)
        );
    }

    public static Ascii4 read(InputStream stream) throws IOException {
        byte a = readOrThrow(stream);
        byte b = readOrThrow(stream);
        byte c = readOrThrow(stream);
        byte d = readOrThrow(stream);
        return new Ascii4(a, b, c, d);
    }

    private static byte readOrThrow(InputStream stream) throws IOException {
        int r = stream.read();
        if (r == -1) throw new EOFException();
        return (byte) r;
    }

    @Override
    public String toString() {
        byte[] data = {a, b, c, d};
        return new String(data, StandardCharsets.US_ASCII);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof String s) {
            byte[] sBytes = s.getBytes(StandardCharsets.US_ASCII);
            return sBytes[0] == a && sBytes[1] == b && sBytes[2] == c && sBytes[3] == d;
        }

        if (!(o instanceof Ascii4 ascii4)) return false;
        return a == ascii4.a && b == ascii4.b && c == ascii4.c && d == ascii4.d;
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c, d);
    }
}

package com.vke.core.font.ttf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TTFReader {

    private final byte[] bytes;
    private final int count;

    private int pos;

    public TTFReader(InputStream is) throws IOException {
        this.bytes = is.readAllBytes();
        this.count = bytes.length;
        this.pos = 0;
    }

    public void skip(int num) {
        if (pos + num > count) throw new IndexOutOfBoundsException(pos + num);
        pos += num;
    }

    public int position() {
        return pos;
    }

    public void position(long pos) {
        if (pos < 0 || pos > count) throw new IndexOutOfBoundsException(pos);
        this.pos = (int) pos;
    }

    public void require(int bytes) {
        if (pos + bytes > count) throw new RuntimeException("Unexpected EOF");
    }

    public int u8() {
        require(1);
        return bytes[pos++] & 0xFF;
    }

    public int u16() {
        return (u8() << 8) | u8();
    }

    public long u32() {
        return ((long) u16() << 16) | u16();
    }

    public long u64() {
        return ((u32() << 32) | u32());
    }

    public byte i8() {
        return (byte) u8();
    }

    public short i16() {
        return (short) u16();
    }

    public int i32() {
        return (int) u32();
    }

    public long i64() {
        return u64();
    }

    public byte[] bytes(int len) {
        byte[] c = Arrays.copyOfRange(bytes, pos, pos + len);
        pos += len;
        return c;
    }

    public double fixed() {
        return i32() / 65536.0;
    }

    public short fword() {
        return i16();
    }

    public String tag() {
        return new String(bytes(4), StandardCharsets.US_ASCII);
    }

    public String ascii(int count) {
        return new String(bytes(count), StandardCharsets.US_ASCII);
    }

}

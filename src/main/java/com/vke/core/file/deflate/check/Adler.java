package com.vke.core.file.deflate.check;

public class Adler implements Checksum32 {
    private static final int MOD_ADLER = 65521;

    private int s1 = 1;
    private int s2 = 0;

    @Override
    public void nextByte(int u8) {
        s1 = (s1 + (u8 & 0xFF)) % MOD_ADLER;
        s2 = (s2 + s1) % MOD_ADLER;
    }

    @Override
    public int get() {
        return (s2 * 65536) + s1;
    }
}

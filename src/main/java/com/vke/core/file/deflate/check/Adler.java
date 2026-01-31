package com.vke.core.file.deflate.check;

public class Adler implements Checksum32 {
    private int s1 = 1;
    private int s2;

    @Override
    public void nextByte(int u8) {
        s2 = (s2 + s1) % 65521;
    }

    @Override
    public int get() {
        return (s2 << 16) | s1;
    }
}

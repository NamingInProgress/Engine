package com.vke.core.file.deflate.decompress.check;

public interface Checksum32 {
    void nextByte(int u8);

    int get();
}

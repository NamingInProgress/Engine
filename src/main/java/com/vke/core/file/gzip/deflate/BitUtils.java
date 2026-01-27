package com.vke.core.file.gzip.deflate;

public class BitUtils {
    public static int right(int bits, int n) {
        int shift = 32 - n;
        bits <<= shift;
        return bits >>> shift;
    }
}

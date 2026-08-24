package com.vke.core.file.utils;

public class BitPackerUtils {
    public static int[] unpackU4LE(int source, int n) {
        return unpackUXLE(source, n, 0x0F, 4);
    }

    public static int[] unpackU4BE(int source, int n) {
        return unpackUXBE(source, n, 0x0F, 4);
    }

    public static int[] unpackUXLE(long source, int n, int mask, int shiftBy) {
        int[] out = new int[n];
        for (int i = 0; i < n; i++) {
            out[i] = (int) (source & mask);
            source >>>= shiftBy;
        }
        return out;
    }

    public static int[] unpackUXBE(long source, int n, int mask, int shiftBy) {
        int[] out = new int[n];
        for (int i = n - 1; i >= 0; i--) {
            out[i] = (int) (source & mask);
            source >>>= shiftBy;
        }
        return out;
    }
}

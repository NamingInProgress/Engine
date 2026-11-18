package com.vke.core.file.utils;

public class BitPackerUtils {
    public static int[] unpackU4LE(int source, int n) {
        return unpackUXLE(source, n, 0x0F, 4);
    }

    public static int[] unpackU4BE(int source, int n) {
        return unpackUXBE(source, n, 0x0F, 4);
    }

    public static boolean[] unpackU1(int source, int n) {
        int[] ints = unpackUXLE(source, n, 0x01, 1);
        boolean[] bools = new boolean[n];
        for (int i = 0; i < n; i++) {
            bools[i] = ints[i] == 1;
        }
        return bools;
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

    public static long packUXLE(int[] source, int start, int n, int mask, int shiftBy) {
        long out = 0;
        for (int i = 0; i < n; i++) {
            out |= ((long) (source[start + i] & mask)) << (i * shiftBy);
        }
        return out;
    }

    public static long packUXBE(int[] source, int start, int n, int mask, int shiftBy) {
        long out = 0;
        for (int i = 0; i < n; i++) {
            out |= ((long) (source[start + i] & mask)) << ((n - 1 - i) * shiftBy);
        }
        return out;
    }

    public static void packU16LE(int source, byte[] dst, int start) {
        dst[start]     = (byte) (source & 0xFF);
        dst[start + 1] = (byte) ((source >>> 8) & 0xFF);
    }

    public static void packU16BE(int source, byte[] dst, int start) {
        dst[start]     = (byte) ((source >>> 8) & 0xFF);
        dst[start + 1] = (byte) (source & 0xFF);
    }

    public static void packU32LE(int source, byte[] dst, int start) {
        dst[start]     = (byte) (source & 0xFF);
        dst[start + 1] = (byte) ((source >>> 8) & 0xFF);
        dst[start + 2] = (byte) ((source >>> 16) & 0xFF);
        dst[start + 3] = (byte) ((source >>> 24) & 0xFF);
    }

    public static void packU32BE(int source, byte[] dst, int start) {
        dst[start]     = (byte) ((source >>> 24) & 0xFF);
        dst[start + 1] = (byte) ((source >>> 16) & 0xFF);
        dst[start + 2] = (byte) ((source >>> 8) & 0xFF);
        dst[start + 3] = (byte) (source & 0xFF);
    }

    public static void packU64LE(long source, byte[] dst, int start) {
        for (int i = 0; i < 8; i++) {
            dst[start + i] = (byte) (source & 0xFF);
            source >>>= 8;
        }
    }

    public static void packU64BE(long source, byte[] dst, int start) {
        for (int i = 7; i >= 0; i--) {
            dst[start + i] = (byte) (source & 0xFF);
            source >>>= 8;
        }
    }
}

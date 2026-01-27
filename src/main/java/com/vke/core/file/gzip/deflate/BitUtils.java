package com.vke.core.file.gzip.deflate;

public class BitUtils {
    public static int right(int bits, int n) {
        int shift = 32 - n;
        bits <<= shift;
        return bits >>> shift;
    }

    public static boolean bitsContains(int provided, int wanted) {
        return (provided & wanted) == wanted;
    }

    /**
     * Truncates the given integer so that `left` and `right` boundaries are respected
     * @param v
     * @param left
     * @param right
     * @return the truncated int
     */
    public static int truncateBits(int v, int left, int right) {
        if (left == 0) {
            v >>>= right;
            v <<= right;
            return v;
        }
        if (right == 0) {
            v <<= left;
            v >>>= left;
            return v;
        }
        v <<= left;
        v >>>= left + right;
        v <<= right;
        return v;
    }

    public static int topBits(int bits, int totalBits, int wantBits) {
        return bits >>> (totalBits - wantBits);
    }

    public static int lowBits(int bits, int wantBits) {
        return bits & ((1 << wantBits) - 1);
    }

    public static int reverseBits(int value, int bitLength) {
        int result = 0;
        for (int i = 0; i < bitLength; i++) {
            result = (result << 1) | (value & 1);
            value >>>= 1;
        }
        return result;
    }

    public static String intToBinStr(int i) {
        StringBuilder builder = new StringBuilder();
        for (int j = 32 - 1; j >= 0; j--) {
            int bit = ((1 << j) & i) >>> j;
            builder.append(bit);
        }
        return builder.toString();
    }

    public static void intToBinStr(int i, StringBuilder builder, boolean reverseBits) {
        if (reverseBits) {
            i = Integer.reverse(i);
        }
        for (int j = 32 - 1; j >= 0; j--) {
            int bit = ((1 << j) & i) >>> j;
            builder.append(bit);
        }
    }
}

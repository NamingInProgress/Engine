package com.vke.utils;

public class IntUtils {
    public static int fastCeilDivPositive(int a, int b) {
        return (a + b - 1) / b;
    }

    public static int fastiLogCeil2(int v) {
        //max says this works i trust him
        return 32 - Integer.numberOfLeadingZeros(v - 1);
    }

    public static int fastiLogFloor2(int v) {
        return 31 - Integer.numberOfLeadingZeros(v);
    }

    public static boolean isPowerOf2(int v) {
        return v > 0 && (v & (v - 1)) == 0;
    }
}

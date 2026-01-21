package com.vke.utils;

import java.nio.IntBuffer;

public class Utils {
    public static boolean intsContain(int[] arr, int query) {
        for (int t : arr) {
            if (t == query) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean TsContain(T[] arr, T query) {
        for (T t : arr) {
            if (t.equals(query)) {
                return true;
            }
        }
        return false;
    }

    public static int[] acquireIntArrayFromBuffer(IntBuffer buffer) {
        if (buffer.hasArray()) {
            return buffer.array();
        }
        int size = buffer.limit();
        int[] out = new int[size];
        for (int i = 0; i < size; i++) {
            out[i] = buffer.get(i);
        }
        return out;
    }
}

package com.vke.utils;

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
}

package com.vke.core.ecs;

import java.util.Arrays;

public class EcsUtils {

    // =========================================================================
    // FLOAT
    // =========================================================================

    public static float[] resize(float[] arr, int newSize) {
        return Arrays.copyOf(arr, newSize);
    }

    public static void swap(float[] arr, int a, int b) {
        float tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static void copyFrom(float[] from, float[] to, int fromIndex, int toIndex) {
        to[toIndex] = from[fromIndex];
    }

    public static void copyRange(float[] arr, int from, int to, int length) {
        System.arraycopy(arr, from, arr, to, length);
    }

    // =========================================================================
    // INT
    // =========================================================================

    public static int[] resize(int[] arr, int newSize) {
        return Arrays.copyOf(arr, newSize);
    }

    public static void swap(int[] arr, int a, int b) {
        int tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static void copyFrom(int[] from, int[] to, int fromIndex, int toIndex) {
        to[toIndex] = from[fromIndex];
    }

    public static void copyRange(int[] arr, int from, int to, int length) {
        System.arraycopy(arr, from, arr, to, length);
    }

    // =========================================================================
    // LONG
    // =========================================================================

    public static long[] resize(long[] arr, int newSize) {
        return Arrays.copyOf(arr, newSize);
    }

    public static void swap(long[] arr, int a, int b) {
        long tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static void copyFrom(long[] from, long[] to, int fromIndex, int toIndex) {
        to[toIndex] = from[fromIndex];
    }

    public static void copyRange(long[] arr, int from, int to, int length) {
        System.arraycopy(arr, from, arr, to, length);
    }

    // =========================================================================
    // DOUBLE
    // =========================================================================

    public static double[] resize(double[] arr, int newSize) {
        return Arrays.copyOf(arr, newSize);
    }

    public static void swap(double[] arr, int a, int b) {
        double tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static void copyFrom(double[] from, double[] to, int fromIndex, int toIndex) {
        to[toIndex] = from[fromIndex];
    }

    public static void copyRange(double[] arr, int from, int to, int length) {
        System.arraycopy(arr, from, arr, to, length);
    }

    // =========================================================================
    // BYTE
    // =========================================================================

    public static byte[] resize(byte[] arr, int newSize) {
        return Arrays.copyOf(arr, newSize);
    }

    public static void swap(byte[] arr, int a, int b) {
        byte tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static void copyFrom(byte[] from, byte[] to, int fromIndex, int toIndex) {
        to[toIndex] = from[fromIndex];
    }

    public static void copyRange(byte[] arr, int from, int to, int length) {
        System.arraycopy(arr, from, arr, to, length);
    }

    // =========================================================================
    // SHORT
    // =========================================================================

    public static short[] resize(short[] arr, int newSize) {
        return Arrays.copyOf(arr, newSize);
    }

    public static void swap(short[] arr, int a, int b) {
        short tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static void copyFrom(short[] from, short[] to, int fromIndex, int toIndex) {
        to[toIndex] = from[fromIndex];
    }

    public static void copyRange(short[] arr, int from, int to, int length) {
        System.arraycopy(arr, from, arr, to, length);
    }

    // =========================================================================
    // BOOLEAN
    // =========================================================================

    public static boolean[] resize(boolean[] arr, int newSize) {
        return Arrays.copyOf(arr, newSize);
    }

    public static void swap(boolean[] arr, int a, int b) {
        boolean tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static void copyFrom(boolean[] from, boolean[] to, int fromIndex, int toIndex) {
        to[toIndex] = from[fromIndex];
    }

    public static void copyRange(boolean[] arr, int from, int to, int length) {
        System.arraycopy(arr, from, arr, to, length);
    }

    // =========================================================================
    // CHAR
    // =========================================================================

    public static char[] resize(char[] arr, int newSize) {
        return Arrays.copyOf(arr, newSize);
    }

    public static void swap(char[] arr, int a, int b) {
        char tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static void copyFrom(char[] from, char[] to, int fromIndex, int toIndex) {
        to[toIndex] = from[fromIndex];
    }

    public static void copyRange(char[] arr, int from, int to, int length) {
        System.arraycopy(arr, from, arr, to, length);
    }
}

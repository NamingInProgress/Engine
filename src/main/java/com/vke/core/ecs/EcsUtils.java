package com.vke.core.ecs;

import java.util.Arrays;

public class EcsUtils {

    // =========================================================================
    // FLOAT
    // =========================================================================

    public static float[] resize(float[] arr, int newSize) {
        return Arrays.copyOf(arr, newSize);
    }

    public static float[] resize(float[] arr, int newSize, int span) {
        return Arrays.copyOf(arr, newSize * span);
    }

    public static void swap(float[] arr, int a, int b) {
        float tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static void swap(float[] arr, int a, int b, int span) {
        float[] tmp = new float[span];
        System.arraycopy(arr, a, tmp, 0, span);
        System.arraycopy(arr, b, arr, a, span);
        System.arraycopy(tmp, 0, arr, b, span);
    }

    public static void copyFrom(float[] from, float[] to, int fromIndex, int toIndex) {
        to[toIndex] = from[fromIndex];
    }

    public static void copyFrom(float[] from, float[] to, int fromIndex, int toIndex, int span) {
        System.arraycopy(from, fromIndex, to, toIndex, span);
    }

    public static void copyRange(float[] arr, int from, int to, int length) {
        System.arraycopy(arr, from, arr, to, length);
    }

    public static void copyRange(float[] arr, int from, int to, int length, int span) {
        System.arraycopy(arr, from, arr, to, length * span);
    }

    // =========================================================================
    // INT
    // =========================================================================

    public static int[] resize(int[] arr, int newSize) {
        return Arrays.copyOf(arr, newSize);
    }

    public static int[] resize(int[] arr, int newSize, int span) {
        return Arrays.copyOf(arr, newSize * span);
    }

    public static void swap(int[] arr, int a, int b) {
        int tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static void swap(int[] arr, int a, int b, int span) {
        int[] tmp = new int[span];
        System.arraycopy(arr, a, tmp, 0, span);
        System.arraycopy(arr, b, arr, a, span);
        System.arraycopy(tmp, 0, arr, b, span);
    }

    public static void copyFrom(int[] from, int[] to, int fromIndex, int toIndex) {
        to[toIndex] = from[fromIndex];
    }

    public static void copyFrom(int[] from, int[] to, int fromIndex, int toIndex, int span) {
        System.arraycopy(from, fromIndex, to, toIndex, span);
    }

    public static void copyRange(int[] arr, int from, int to, int length) {
        System.arraycopy(arr, from, arr, to, length);
    }

    public static void copyRange(int[] arr, int from, int to, int length, int span) {
        System.arraycopy(arr, from, arr, to, length * span);
    }

    // =========================================================================
    // LONG
    // =========================================================================

    public static long[] resize(long[] arr, int newSize) {
        return Arrays.copyOf(arr, newSize);
    }

    public static long[] resize(long[] arr, int newSize, int span) {
        return Arrays.copyOf(arr, newSize * span);
    }

    public static void swap(long[] arr, int a, int b) {
        long tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static void swap(long[] arr, int a, int b, int span) {
        long[] tmp = new long[span];
        System.arraycopy(arr, a, tmp, 0, span);
        System.arraycopy(arr, b, arr, a, span);
        System.arraycopy(tmp, 0, arr, b, span);
    }

    public static void copyFrom(long[] from, long[] to, int fromIndex, int toIndex) {
        to[toIndex] = from[fromIndex];
    }

    public static void copyFrom(long[] from, long[] to, int fromIndex, int toIndex, int span) {
        System.arraycopy(from, fromIndex, to, toIndex, span);
    }

    public static void copyRange(long[] arr, int from, int to, int length) {
        System.arraycopy(arr, from, arr, to, length);
    }

    public static void copyRange(long[] arr, int from, int to, int length, int span) {
        System.arraycopy(arr, from, arr, to, length * span);
    }

    // =========================================================================
    // DOUBLE
    // =========================================================================

    public static double[] resize(double[] arr, int newSize) {
        return Arrays.copyOf(arr, newSize);
    }

    public static double[] resize(double[] arr, int newSize, int span) {
        return Arrays.copyOf(arr, newSize * span);
    }

    public static void swap(double[] arr, int a, int b) {
        double tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static void swap(double[] arr, int a, int b, int span) {
        double[] tmp = new double[span];
        System.arraycopy(arr, a, tmp, 0, span);
        System.arraycopy(arr, b, arr, a, span);
        System.arraycopy(tmp, 0, arr, b, span);
    }

    public static void copyFrom(double[] from, double[] to, int fromIndex, int toIndex) {
        to[toIndex] = from[fromIndex];
    }

    public static void copyFrom(double[] from, double[] to, int fromIndex, int toIndex, int span) {
        System.arraycopy(from, fromIndex, to, toIndex, span);
    }

    public static void copyRange(double[] arr, int from, int to, int length) {
        System.arraycopy(arr, from, arr, to, length);
    }

    public static void copyRange(double[] arr, int from, int to, int length, int span) {
        System.arraycopy(arr, from, arr, to, length * span);
    }

    // =========================================================================
    // BYTE
    // =========================================================================

    public static byte[] resize(byte[] arr, int newSize) {
        return Arrays.copyOf(arr, newSize);
    }

    public static byte[] resize(byte[] arr, int newSize, int span) {
        return Arrays.copyOf(arr, newSize * span);
    }

    public static void swap(byte[] arr, int a, int b) {
        byte tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static void swap(byte[] arr, int a, int b, int span) {
        byte[] tmp = new byte[span];
        System.arraycopy(arr, a, tmp, 0, span);
        System.arraycopy(arr, b, arr, a, span);
        System.arraycopy(tmp, 0, arr, b, span);
    }

    public static void copyFrom(byte[] from, byte[] to, int fromIndex, int toIndex) {
        to[toIndex] = from[fromIndex];
    }

    public static void copyFrom(byte[] from, byte[] to, int fromIndex, int toIndex, int span) {
        System.arraycopy(from, fromIndex, to, toIndex, span);
    }

    public static void copyRange(byte[] arr, int from, int to, int length) {
        System.arraycopy(arr, from, arr, to, length);
    }

    public static void copyRange(byte[] arr, int from, int to, int length, int span) {
        System.arraycopy(arr, from, arr, to, length * span);
    }

    // =========================================================================
    // SHORT
    // =========================================================================

    public static short[] resize(short[] arr, int newSize) {
        return Arrays.copyOf(arr, newSize);
    }

    public static short[] resize(short[] arr, int newSize, int span) {
        return Arrays.copyOf(arr, newSize * span);
    }

    public static void swap(short[] arr, int a, int b) {
        short tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static void swap(short[] arr, int a, int b, int span) {
        short[] tmp = new short[span];
        System.arraycopy(arr, a, tmp, 0, span);
        System.arraycopy(arr, b, arr, a, span);
        System.arraycopy(tmp, 0, arr, b, span);
    }

    public static void copyFrom(short[] from, short[] to, int fromIndex, int toIndex) {
        to[toIndex] = from[fromIndex];
    }

    public static void copyFrom(short[] from, short[] to, int fromIndex, int toIndex, int span) {
        System.arraycopy(from, fromIndex, to, toIndex, span);
    }

    public static void copyRange(short[] arr, int from, int to, int length) {
        System.arraycopy(arr, from, arr, to, length);
    }

    public static void copyRange(short[] arr, int from, int to, int length, int span) {
        System.arraycopy(arr, from, arr, to, length * span);
    }

    // =========================================================================
    // BOOLEAN
    // =========================================================================

    public static boolean[] resize(boolean[] arr, int newSize) {
        return Arrays.copyOf(arr, newSize);
    }

    public static boolean[] resize(boolean[] arr, int newSize, int span) {
        return Arrays.copyOf(arr, newSize * span);
    }

    public static void swap(boolean[] arr, int a, int b) {
        boolean tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static void swap(boolean[] arr, int a, int b, int span) {
        boolean[] tmp = new boolean[span];
        System.arraycopy(arr, a, tmp, 0, span);
        System.arraycopy(arr, b, arr, a, span);
        System.arraycopy(tmp, 0, arr, b, span);
    }

    public static void copyFrom(boolean[] from, boolean[] to, int fromIndex, int toIndex) {
        to[toIndex] = from[fromIndex];
    }

    public static void copyFrom(boolean[] from, boolean[] to, int fromIndex, int toIndex, int span) {
        System.arraycopy(from, fromIndex, to, toIndex, span);
    }

    public static void copyRange(boolean[] arr, int from, int to, int length) {
        System.arraycopy(arr, from, arr, to, length);
    }

    public static void copyRange(boolean[] arr, int from, int to, int length, int span) {
        System.arraycopy(arr, from, arr, to, length * span);
    }

    // =========================================================================
    // CHAR
    // =========================================================================

    public static char[] resize(char[] arr, int newSize) {
        return Arrays.copyOf(arr, newSize);
    }

    public static char[] resize(char[] arr, int newSize, int span) {
        return Arrays.copyOf(arr, newSize * span);
    }

    public static void swap(char[] arr, int a, int b) {
        char tmp = arr[a];
        arr[a] = arr[b];
        arr[b] = tmp;
    }

    public static void swap(char[] arr, int a, int b, int span) {
        char[] tmp = new char[span];
        System.arraycopy(arr, a, tmp, 0, span);
        System.arraycopy(arr, b, arr, a, span);
        System.arraycopy(tmp, 0, arr, b, span);
    }

    public static void copyFrom(char[] from, char[] to, int fromIndex, int toIndex) {
        to[toIndex] = from[fromIndex];
    }

    public static void copyFrom(char[] from, char[] to, int fromIndex, int toIndex, int span) {
        System.arraycopy(from, fromIndex, to, toIndex, span);
    }

    public static void copyRange(char[] arr, int from, int to, int length) {
        System.arraycopy(arr, from, arr, to, length);
    }

    public static void copyRange(char[] arr, int from, int to, int length, int span) {
        System.arraycopy(arr, from, arr, to, length * span);
    }
}

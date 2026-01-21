package com.vke.api.serializer;

import com.vke.utils.exception.SaveException;

public interface Saver {
    void saveByte(byte v) throws SaveException;

    default void saveBoolean(boolean v) throws SaveException {
        saveBits(1, (v ? 1 : 0));
    }

    default void saveShort(short v) throws SaveException {
        saveByte((byte) (v >>> 8));
        saveByte((byte) v);
    }

    default void saveChar(char v) throws SaveException {
        saveShort((short) v);
    }

    default void saveInt(int v) throws SaveException {
        saveByte((byte) (v >>> 24));
        saveByte((byte) (v >>> 16));
        saveByte((byte) (v >>> 8));
        saveByte((byte) v);
    }

    default void saveLong(long v) throws SaveException {
        for (int i = 7; i >= 0; i--) {
            saveByte((byte) (v >>> (i * 8)));
        }
    }

    default void saveFloat(float v) throws SaveException {
        saveInt(Float.floatToIntBits(v));
    }

    default void saveDouble(double v) throws SaveException {
        saveLong(Double.doubleToLongBits(v));
    }

    default void saveByteArray(byte[] v) throws SaveException {
        saveInt(v.length);
        for (byte b : v) saveByte(b);
    }

    default void saveBooleanArray(boolean[] v) throws SaveException {
        saveInt(v.length);
        for (boolean b : v) saveBoolean(b);
    }

    default void saveShortArray(short[] v) throws SaveException {
        saveInt(v.length);
        for (short s : v) saveShort(s);
    }

    default void saveCharArray(char[] v) throws SaveException {
        saveInt(v.length);
        for (char c : v) saveChar(c);
    }

    default void saveIntArray(int[] v) throws SaveException {
        saveInt(v.length);
        for (int i : v) saveInt(i);
    }

    default void saveLongArray(long[] v) throws SaveException {
        saveInt(v.length);
        for (long l : v) saveLong(l);
    }

    default void saveFloatArray(float[] v) throws SaveException {
        saveInt(v.length);
        for (float f : v) saveFloat(f);
    }

    default void saveDoubleArray(double[] v) throws SaveException {
        saveInt(v.length);
        for (double d : v) saveDouble(d);
    }

    //for efficient saving this may be overridden
    default void saveBits(int n, int bitMap) throws SaveException {
        if (n <= 8) {
            saveByte((byte) bitMap);
        } else if (n <= 16) {
            saveByte((byte) bitMap);
            saveByte((byte) (bitMap >> 8));
        } else if (n <= 24) {
            saveByte((byte) bitMap);
            saveByte((byte) (bitMap >> 8));
            saveByte((byte) (bitMap >> 16));
        } else {
            saveInt(bitMap);
        }
    }
}
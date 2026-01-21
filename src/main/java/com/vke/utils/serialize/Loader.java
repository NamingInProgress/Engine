package com.vke.utils.serialize;

import com.vke.utils.serialize.exec.LoadException;

public interface Loader {
    byte loadByte() throws LoadException;

    default boolean loadBoolean() throws LoadException {
        return (loadBits(1) & 1) == 1;
    }

    default short loadShort() throws LoadException {
        return (short) (
                ((loadByte() & 0xFF) << 8) |
                        (loadByte() & 0xFF)
        );
    }

    default char loadChar() throws LoadException {
        return (char) loadShort();
    }

    default int loadInt() throws LoadException {
        return
                ((loadByte() & 0xFF) << 24) |
                        ((loadByte() & 0xFF) << 16) |
                        ((loadByte() & 0xFF) << 8)  |
                        (loadByte() & 0xFF);
    }

    default long loadLong() throws LoadException {
        long v = 0;
        for (int i = 7; i >= 0; i--) {
            v |= ((long) (loadByte() & 0xFF)) << (i * 8);
        }
        return v;
    }

    default float loadFloat() throws LoadException {
        return Float.intBitsToFloat(loadInt());
    }

    default double loadDouble() throws LoadException {
        return Double.longBitsToDouble(loadLong());
    }

    default byte[] loadByteArray() throws LoadException {
        int len = loadInt();
        byte[] out = new byte[len];
        for (int i = 0; i < len; i++) {
            out[i] = loadByte();
        }
        return out;
    }

    default boolean[] loadBooleanArray() throws LoadException {
        int len = loadInt();
        boolean[] out = new boolean[len];
        for (int i = 0; i < len; i++) {
            out[i] = loadBoolean();
        }
        return out;
    }

    default short[] loadShortArray() throws LoadException {
        int len = loadInt();
        short[] out = new short[len];
        for (int i = 0; i < len; i++) {
            out[i] = loadShort();
        }
        return out;
    }

    default char[] loadCharArray() throws LoadException {
        int len = loadInt();
        char[] out = new char[len];
        for (int i = 0; i < len; i++) {
            out[i] = loadChar();
        }
        return out;
    }

    default int[] loadIntArray() throws LoadException {
        int len = loadInt();
        int[] out = new int[len];
        for (int i = 0; i < len; i++) {
            out[i] = loadInt();
        }
        return out;
    }

    default long[] loadLongArray() throws LoadException {
        int len = loadInt();
        long[] out = new long[len];
        for (int i = 0; i < len; i++) {
            out[i] = loadLong();
        }
        return out;
    }

    default float[] loadFloatArray() throws LoadException {
        int len = loadInt();
        float[] out = new float[len];
        for (int i = 0; i < len; i++) {
            out[i] = loadFloat();
        }
        return out;
    }

    default double[] loadDoubleArray() throws LoadException {
        int len = loadInt();
        double[] out = new double[len];
        for (int i = 0; i < len; i++) {
            out[i] = loadDouble();
        }
        return out;
    }

    // for efficient loading this may be overridden
    default int loadBits(int n) throws LoadException {
        if (n <= 8) {
            return loadByte() & 0xFF;
        } else if (n <= 16) {
            return
                    (loadByte() & 0xFF) |
                            ((loadByte() & 0xFF) << 8);
        } else if (n <= 24) {
            return
                    (loadByte() & 0xFF) |
                            ((loadByte() & 0xFF) << 8) |
                            ((loadByte() & 0xFF) << 16);
        } else {
            return loadInt();
        }
    }
}
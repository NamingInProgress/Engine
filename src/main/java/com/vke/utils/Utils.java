package com.vke.utils;

import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
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

    public static byte[] acquireByteArrayFromBuffer(ByteBuffer buffer) {
        if (buffer.hasArray()) {
            return buffer.array();
        }
        int size = buffer.limit();
        byte[] out = new byte[size];
        for (int i = 0; i < size; i++) {
            out[i] = buffer.get(i);
        }
        return out;
    }

    public static ByteBuffer ensureCStr(ByteBuffer everyOtherLanguageString) {
        int len = everyOtherLanguageString.limit();
        if ((everyOtherLanguageString.get(len - 1) & 0xFF) == '\0') {
            return everyOtherLanguageString;
        }

        ByteBuffer bigger = MemoryUtil.memRealloc(everyOtherLanguageString, len + 1);
        bigger.put(len, (byte) '\0');
        return bigger;
    }

    public static byte[] readAllBytesAndClose(InputStream stream) throws IOException {
        try (stream) {
            return stream.readAllBytes();
        }
    }
}

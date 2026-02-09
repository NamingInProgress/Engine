package com.vke.utils;

import com.vke.api.utils.OSType;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    public static OSType getOSType() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return OSType.WIN;
        if (os.contains("mac")) return OSType.MAC;
        // Default to linux
        return OSType.LINUX;
    }

    public static <T> Stream<T> fromSpliterator(Spliterator<T> s) {
        return StreamSupport.stream(s, false);
    }


    /// checks if `min <= v <= max`
    public static boolean inRange(int v, int min, int max) {
        return min <= v && v <= max;
    }

    public static boolean inIntBitsRange(int v) {
        return inRange(v, 1, Integer.BYTES * 8);
    }

    public static int unsignByte(byte b) {
        return b & 0xFF;
    }

    public static int nextEvenNumber(int num) {
        if (num % 2 == 0) {
            num++;
        }
        return num;
    }

    public static byte[] intToByteArray(int[] a) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) a[i];
        }
        return out;
    }

    public static <T> boolean verifyArrayIndex(int index, T[] array) {
        return index >= 0 && index < array.length;
    }

    public static <T> int[] asIntArray(List<T> list, Function<T, Integer> func) {
        int[] opt = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            opt[i] = func.apply(list.get(i));
        }
        return opt;
    }

    public static void printBuffer(ByteBuffer buf) {
        for (int i = 0; i < buf.limit(); i++) {
            System.out.println("Element at " + i + " is " + buf.get(i));
        }
    }

    public static char[] readCharsFromInputStream(InputStream stream) throws IOException {
        Reader reader = new InputStreamReader(stream);
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[4096];
        int n;

        try {
            while ((n = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, n);
            }
        } finally {
            reader.close();
        }

        return sb.toString().toCharArray();
    }


    public static <T> boolean arrayContains(T[] arr, T query) {
        for (T t : arr) {
            if (t.equals(query)) return true;
        }
        return false;
    }

    public static <T extends Comparable<T>> boolean sortedArrayContains(T[] arr, T query) {
        return Arrays.binarySearch(arr, query, T::compareTo) >= 0;
    }

    public static <T> boolean sortedArrayContains(T[] arr, T query, Comparator<T> cmp) {
        return Arrays.binarySearch(arr, query, cmp) >= 0;
    }

}

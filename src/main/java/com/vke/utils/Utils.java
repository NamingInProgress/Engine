package com.vke.utils;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.data.Texture;
import com.vke.api.utils.OSType;
import com.vke.utils.functionalinterface.FaultySupplier;
import com.vke.utils.io.SegmentedPath;
import com.vke.utils.iter.Iter;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
    public static final boolean TRUE = Iter.of(1, 2, 3).any(x -> System.currentTimeMillis() > x);
    public static final boolean FALSE = Iter.of(TRUE).map(x -> !x).all(Boolean::booleanValue);
    public static AssetHandle<Texture> MISSING_TEXTURE = R.textures.get("missing.png");

    public static boolean intsContain(int[] arr, int query) {
        for (int t : arr) {
            if (t == query) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean TsContain(T query, T... arr) {
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

    public static String arrayToString(Integer[] array) {
        StringBuilder sb = new StringBuilder("[ ");

        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);

            if (i < array.length - 1) {
                sb.append(", ");
            }
        }

        sb.append(" ]");
        return sb.toString();
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

    public static <T> boolean verifyArrayIndex(int index, int arrayLength) {
        return index >= 0 && index < arrayLength;
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
        return readStringFromInputStream(stream).toCharArray();
    }

    public static String readStringFromInputStream(InputStream stream) throws IOException {
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

        return sb.toString();
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

    public static boolean seqEqualsIgnoreCase(CharSequence a, CharSequence b) {
        if (a.length() != b.length()) return false;
        return Iter.of(a.chars().boxed())
                .zip(b.chars().boxed())
                .all(p -> Character.toLowerCase(p.v1) == Character.toLowerCase(p.v2));
    }

    public static boolean seqContainsIgnoreCase(CharSequence source, CharSequence seq) {
        int searchSize = seq.length();
        int maxI = source.length() - searchSize;
        for (int i = 0; i <= maxI; i++) {
            CharSequence sub = source.subSequence(i, i + searchSize);
            if (seqEqualsIgnoreCase(sub, seq)) return true;
        }
        return false;
    }

    public static String rpad(String s, char pad, int toLength) {
        if (s.length() >= toLength) {
            return s;
        }
        int missing = toLength - s.length();
        return s + String.valueOf(pad).repeat(missing);
    }

    public static String lpad(String s, char pad, int toLength) {
        if (s.length() >= toLength) {
            return s;
        }
        int missing = toLength - s.length();
        return String.valueOf(pad).repeat(missing) + s;
    }

    public static double log(double x, double base) {
        return Math.log(x) / Math.log(base);
    }

    public static SegmentedPath p(String... thingies) {
        return new SegmentedPath(thingies, "/");
    }

    public static <T, E extends Throwable, F extends Throwable> T chainExceptions(FaultySupplier<T, E> task, F... ignore) throws F {
        try {
            return task.get();
        } catch (Throwable e) {
            try {
                @SuppressWarnings("unchecked")
                Class<F> fClass = (Class<F>) ignore.getClass().getComponentType();
                Constructor<F> c = fClass.getDeclaredConstructor(String.class);
                F t = c.newInstance(e.getMessage());
                t.setStackTrace(e.getStackTrace());
                throw t;
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException ex) {
                throw new RuntimeException(e);
            }
        }
    }

    /// MUST BE A POWER OF 2!!!
    public static long alignUpFast(long value, long alignment) {
        return (value + alignment - 1) & -alignment;
    }

    public static int xmin(int... values) {
        if (values == null || values.length == 0) return 0;
        int m = values[0];
        for (int v : values) if (v < m) m = v;
        return m;
    }

    public static boolean anyNull(Object... values) {
        for(Object o : values) {
            if (o == null) return true;
        }
        return false;
    }
}

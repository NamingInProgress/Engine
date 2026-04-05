package com.vke.core.vz;

public class VzUtils {
    private static final long MAGIC_PRIME = 0x00000100000001b3L;

    public static long FNV1a(byte[] bytes) {
        return FNV1a(bytes, false);
    }

    //https://de.wikipedia.org/wiki/FNV_(Informatik)
    public static long FNV1a(byte[] bytes, boolean reverse) {
        long hash = 0xcbf29ce484222325L;
        if (!reverse) {
            for (byte b : bytes) {
                hash = (hash ^ b) * MAGIC_PRIME;
            }
        } else {
            for (int i = bytes.length - 1; i >= 0; i--) {
                byte b = bytes[i];
                hash = (hash ^ b) * MAGIC_PRIME;
            }
        }
        return hash;
    }
}

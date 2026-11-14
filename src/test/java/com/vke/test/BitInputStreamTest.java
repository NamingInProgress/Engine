package com.vke.test;

import com.vke.core.file.deflate.decompress.BitUtils;
import com.vke.core.file.io.bit.BitOrdering;
import com.vke.core.file.io.bit.input.BitInputStream;
import com.vke.core.file.io.bit.input.ShittyBitInputStream;
import com.vke.core.file.io.bit.input.FastBitInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class BitInputStreamTest {

    public static void main(String[] args) throws IOException {
        System.out.println("Running Comprehensive BitInputStream Test Suite...");

        testSequence(BitOrdering.LSB_FIRST);
        testSequence(BitOrdering.MSB_FIRST);

        testMixedPeekAndRead(BitOrdering.LSB_FIRST);
        testMixedPeekAndRead(BitOrdering.MSB_FIRST);

        testByteAlignmentAndReadBytes();
        testRandomizedFuzzing(1337);

        System.out.println("All tests passed successfully!");
    }

    private static void testSequence(BitOrdering ordering) throws IOException {
        byte[] data = new byte[] {
                (byte) 0xAA, (byte) 0x55, (byte) 0xFF, (byte) 0x00,
                0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC, (byte) 0xDE, (byte) 0xF0
        };

        BitInputStream b1 = new ShittyBitInputStream(new ByteArrayInputStream(data.clone()));
        b1.setOrdering(ordering);

        BitInputStream b2 = new FastBitInputStream(new ByteArrayInputStream(data.clone()));
        b2.setOrdering(ordering);

        // Small reads, weird bit lengths
        testEquals(b1, b2, 1);
        testEquals(b1, b2, 3);
        testEquals(b1, b2, 7);
        testEquals(b1, b2, 12);

        // Peek tests crossing register/byte borders
        testPeekEquals(b1, b2, 16);
        testEquals(b1, b2, 16); // consume them

        // Larger reads up to 32 bits
        testEquals(b1, b2, 24);
        testEquals(b1, b2, 31);

        System.out.println("Passed sequence test for ordering: " + ordering);
    }

    private static void testMixedPeekAndRead(BitOrdering ordering) throws IOException {
        byte[] data = new byte[] { (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF, 0x11, 0x22, 0x33, 0x44 };

        BitInputStream b1 = new ShittyBitInputStream(new ByteArrayInputStream(data.clone()));
        b1.setOrdering(ordering);

        BitInputStream b2 = new FastBitInputStream(new ByteArrayInputStream(data.clone()));
        b2.setOrdering(ordering);

        // Peek, peek deeper, read less, peek again
        testPeekEquals(b1, b2, 5);
        testPeekEquals(b1, b2, 10);
        testEquals(b1, b2, 4); // consume partial peek
        testPeekEquals(b1, b2, 14);
        testEquals(b1, b2, 14); // consume rest of peek

        System.out.println("Passed mixed peek/read test for ordering: " + ordering);
    }

    private static void testByteAlignmentAndReadBytes() throws IOException {
        byte[] data = new byte[] {
                0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10
        };

        BitInputStream b1 = new ShittyBitInputStream(new ByteArrayInputStream(data.clone()));
        b1.setOrdering(BitOrdering.LSB_FIRST);

        BitInputStream b2 = new FastBitInputStream(new ByteArrayInputStream(data.clone()));
        b2.setOrdering(BitOrdering.LSB_FIRST);

        // Read a few bits so we are unaligned
        b1.readBits(3);
        b2.readBits(3);

        // Align to byte
        b1.alignToByte();
        b2.alignToByte();

        // Read raw bytes (like uncompressed DEFLATE block payload)
        byte[] buf1 = new byte[5];
        byte[] buf2 = new byte[5];

        int r1 = b1.readBytes(buf1, 0, 5);
        int r2 = b2.readBytes(buf2, 0, 5);

        if (r1 != r2 || !Arrays.equals(buf1, buf2)) {
            throw new RuntimeException("readBytes failed! Expected: " + Arrays.toString(buf1) + ", Got: " + Arrays.toString(buf2));
        }

        System.out.println("Passed byte alignment and readBytes test");
    }

    private static void testRandomizedFuzzing(long seed) throws IOException {
        Random rand = new Random(seed);
        byte[] data = new byte[8000];
        rand.nextBytes(data);

        BitInputStream b1 = new ShittyBitInputStream(new ByteArrayInputStream(data.clone()));
        b1.setOrdering(BitOrdering.LSB_FIRST);

        BitInputStream b2 = new FastBitInputStream(new ByteArrayInputStream(data.clone()));
        b2.setOrdering(BitOrdering.LSB_FIRST);

        for (int i = 0; i < 4000; i++) {
            int n = rand.nextInt(32) + 1; // Read 1 to 25 bits
            boolean isPeek = rand.nextBoolean();
            if (i == 3972   ) {
                System.out.println();
            }

            try {
                int a, b;
                if (isPeek) {
                    a = b1.peekBits(n);
                    b = b2.peekBits(n);
                } else {
                    a = b1.readBits(n);
                    b = b2.readBits(n);
                }
                if (a != b) {
                    throw new RuntimeException(String.format("Fuzz mismatch at op %d (%s %d bits): expected %s, got %s",
                            i, isPeek ? "peek" : "read", n,
                            bin(a), bin(b)));
                }
            } catch (IOException e) {
                // If stream hits EOF, both should throw similarly; if one throws and the other doesn't, that's a bug
                break;
            }
        }
        System.out.println("Passed randomized fuzzing test (" + seed + ")");
    }

    private static void testEquals(BitInputStream b1, BitInputStream b2, int n) throws IOException {
        int a = b1.readBits(n);
        int b = b2.readBits(n);
        if (a != b) {
            throw new RuntimeException(String.format("readBits(%d) failed: %s != %s",
                    n, bin(a), bin(b)));
        }
    }

    private static void testPeekEquals(BitInputStream b1, BitInputStream b2, int n) throws IOException {
        int a = b1.peekBits(n);
        int b = b2.peekBits(n);
        if (a != b) {
            throw new RuntimeException(String.format("peekBits(%d) failed: %s != %s",
                    n, bin(a), bin(b)));
        }
    }

    private static String bin(int val) {
        StringBuilder sb = new StringBuilder();
        BitUtils.intToBinStr(val, sb, false);
        return sb.toString();
    }
}
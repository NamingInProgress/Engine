package com.vke.test.deFUCKINGflate;

import com.vke.core.file.deflate.compress.lz77.SlidingWindow;

import java.util.*;
import java.io.*;

public class SlidingWindowTest {

    private static boolean isMatch(long symbol) {
        return (symbol & (1L << 63)) != 0;
    }

    private static int getDistance(long symbol) {
        return (int) (symbol & 0xFFFFFFFFL);
    }

    private static int getLength(long symbol) {
        return (int) ((symbol >>> 32) & 0xFF);
    }

    public static void main(String[] args) throws Exception {
        testReplay("abcabcabcabcabc");
        testReplay("aaaaaaaaaaaaaaaaaaaa");
        testReplay("abcdefghijklmnopqrstuvwxyz");
        testReplay("abababababababababab");
        testReplay(generateRandom(1000, 0));

        System.out.println("All tests passed.");
    }

    private static void testReplay(String inputString) throws Exception {
        testReplay(inputString.getBytes("UTF-8"));
    }

    private static void testReplay(byte[] input) {
        int windowSize = 1 << 15; // 32KB
        SlidingWindow window = new SlidingWindow(windowSize);

        ByteArrayOutputStream reconstructed = new ByteArrayOutputStream();

        int position = 0;

        while (position < input.length) {

            int remaining = input.length - position;
            byte[] lookahead = Arrays.copyOfRange(input, position, input.length);

            long symbol = window.nextSymbol(64, lookahead, position, remaining);

            if (isMatch(symbol)) {
                int length = getLength(symbol);
                int distance = getDistance(symbol);

                System.out.printf(
                        "Match found at input pos %d: length=%d, distance=%d\n",
                        position, length, distance
                );

                // Simulate decompression
                byte[] current = reconstructed.toByteArray();
                int start = current.length - distance;

                for (int i = 0; i < length; i++) {
                    byte b = current[start + i];
                    reconstructed.write(b);
                    window.processByte(b);
                }

                position += length;

            } else {
                byte literal = (byte) (symbol & 0xFF);

                System.out.printf(
                        "Literal at input pos %d: '%c' (0x%02X)\n",
                        position, (char) literal, literal
                );

                reconstructed.write(literal);
                window.processByte(literal);

                position++;
            }
        }

        byte[] output = reconstructed.toByteArray();

        if (!Arrays.equals(input, output)) {
            throw new AssertionError("Replay failed!\n"
                    + "Original:     " + Arrays.toString(input) + "\n"
                    + "Reconstructed:" + Arrays.toString(output));
        }
    }

    private static String generateRandom(int size, long seed) {
        Random r = new Random(seed);
        byte[] data = new byte[size];
        r.nextBytes(data);
        return new String(data);
    }
}

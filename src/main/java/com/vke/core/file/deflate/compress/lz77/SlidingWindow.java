package com.vke.core.file.deflate.compress.lz77;

import com.vke.utils.Utils;

import java.util.Arrays;

public class SlidingWindow {
    private static final int HASH_BITS = 16;
    private static final int HASH_SIZE = 1 << HASH_BITS;
    private static final int HASH_MASK = HASH_SIZE - 1;

    private final int size;
    private final int WINDOW_MASK;

    private final int[] head;
    private final int[] prev;

    private final byte[] window;
    private int position;
    private byte prev1, prev2;

    public SlidingWindow(int size) throws IllegalArgumentException {
        double log = Utils.log(size, 2);
        if (log != (int) log) {
            throw new IllegalArgumentException(size + " is not a power of 2 and can therefore not be used as the SlidingWindow size!");
        }
        this.size = size;
        this.WINDOW_MASK = size - 1;

        this.head = new int[HASH_SIZE];
        Arrays.fill(this.head, -1);
        this.prev = new int[size];
        Arrays.fill(this.prev, -1);

        this.window = new byte[size];
    }

    private int hash(byte a, byte b, byte c) {
        int h = ((a & 0xFF) << 16) |
                ((b & 0xFF) << 8)  |
                (c & 0xFF);
        return h & HASH_MASK;
    }

    public void processByte(byte b) {
        int index = position & WINDOW_MASK;
        if (position >= 2) {
            int hash = hash(prev2, prev1, b);
            int previousPosition = this.head[hash];
            this.prev[index] = previousPosition;
            this.head[hash] = position;
        }
        prev2 = prev1;
        prev1 = b;
        window[index] = b;
        position++;
    }

    /**
     * lookahead[0] is the current byte to process
     * returns 0-255 for literals
     * if the MSB is 1, then this is a length/distance pair being:
     * distance: bit 1 - 32
     * length: bit 33 - 48
     * @return
     */
    public long nextSymbol(int maxChainChecks, byte[] lookahead, int start, int length) {
        if(true) return lookahead[start] & 0xFF;
        int maxLength = Math.min(length - start, 258);
        int hash = hash(prev2, prev1, lookahead[start]);
        int chainPart = head[hash];

        int bestLength = 0;
        int bestStart = 0;

        int chainChecks = 0;
        while (chainPart != -1 && chainChecks < maxChainChecks && position - chainPart <= size) {
            int chainLength = 0;
            int chainStart = chainPart;
            int queryIndex = 0;
            while (chainLength < maxLength) {
                int toCompare = window[(queryIndex + chainStart) & WINDOW_MASK];
                if (toCompare != lookahead[queryIndex + start]) break;

                chainLength++;
                queryIndex++;
            }

            if (chainLength > bestLength) {
                bestLength = chainLength;
                bestStart = chainStart;

                if (chainLength == 258) break;
            }

            chainPart = prev[chainPart & WINDOW_MASK];
            chainChecks++;
        }

        if (bestLength >= 3) {
            int distance = position - bestStart;
            long result = distance & 0xFFFFFFFFL;
            result |= ((long) bestLength << 32);
            result |= (1L << 63);
            return result;
        }

        return lookahead[start] & 0xFFL;
    }
}

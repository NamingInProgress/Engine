package com.vke.core.file.gzip.deflate;

import com.carrotsearch.hppc.IntArrayDeque;

public class BitQueue {
    private static final int BITS = Integer.BYTES * 8;

    private final IntArrayDeque buffer;
    private int activeBits;
    //how many bits are set in activeBits
    private int cursor;
    //how many bits are still set in the last value of buffer
    private int bufferCursor;

    public BitQueue() {
        buffer = new IntArrayDeque();
        cursor = 0;
    }

    public void add(int bits, int n) {
        int space = remainingActive();
        if (space >= n) {
            int shifted = bits << cursor;
            activeBits |= shifted;
        } else if (space > 0) {

        } else {
            int currentInt = buffer.getLast();
        }
    }

    public int poll(int n) {
        int result;
        int remainingActive = cursor;
        if (remainingActive >= n) {
            result = BitUtils.right(activeBits, n);
            activeBits >>>= n;
            cursor -= n;
        } else if (remainingActive > 0) {
            //shut tf up intelliJ i want my split1 variable
            int split1 = remainingActive;
            int split2 = n - split1;
            int right = activeBits;
            activeBits = buffer.removeFirst();
            int left = BitUtils.right(activeBits, split2);
            cursor = BITS - n;
            activeBits >>>= n;
            result = (left << split1) | right;
        } else {
            activeBits = buffer.removeFirst();
            cursor = BITS - n;
            result = BitUtils.right(activeBits, n);
            activeBits >>>= n;
        }

        //fill up activeBits again with the next value from buffer if it exists
        if (!buffer.isEmpty()) {
            int toFill = BITS - cursor;
            if (toFill == BITS) {
                //aka empty activeBits
                //strat is to simply set it to a new value
                if (buffer.size() == 1) {
                    //the buffer only has 1 (partial) int left!
                    if (bufferCursor == BITS) {
                        //the last value is literally an entire int
                        activeBits = buffer.removeFirst();
                        cursor = BITS;
                    } else if (bufferCursor > 0) {
                        int partial = buffer.removeFirst();
                        int canFill = bufferCursor;
                        activeBits = BitUtils.right(partial, canFill);
                        cursor += canFill;
                        partial >>>= canFill;
                        buffer.addFirst(partial);
                    } else {
                        //ill implement this case even tho it shouldnt happen
                        //removes the empty int
                        buffer.removeFirst();
                        activeBits = 0;
                        cursor = 0;
                    }
                } else {
                    //the buffer has enough bits left
                    activeBits = buffer.removeFirst();
                }
            }
        }
        return result;
    }

    private int remainingActive() {
        return BITS - cursor;
    }

    public int remaining() {
        return (buffer.size() - 1) * 8 + cursor + bufferCursor;
    }
}

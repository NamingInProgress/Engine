package com.vke.core.file.gzip.io.bit;

import com.vke.core.file.gzip.deflate.BitUtils;

import java.io.IOException;
import java.util.LinkedList;

public class BitQueue {
    private final LinkedList<Bits> buffer;
    private int remainingLen;
    private int backupLen;

    public BitQueue() {
        buffer = new LinkedList<>();
        remainingLen = 0;
    }

    public void add(int bits, int n) {
        buffer.addLast(new Bits(bits, n));
        remainingLen += n;
    }

    public int poll(int n) throws IOException {
        if (n < 0 || n > 32) {
            throw new IOException("n must be between 0 and 32");
        }

        int result = 0;
        int collected = 0;

        while (collected < n) {
            if (buffer.isEmpty()) {
                throw new IOException("Not enough bits in BitQueue");
            }

            Bits current = buffer.getFirst();
            int take = Math.min(n - collected, current.len);

            int extracted = current.extract(take);

            result |= extracted << collected;
            collected += take;

            if (current.len == 0) {
                buffer.removeFirst();
            }
        }

        remainingLen -= n;

        return result;
    }

    public int get(int n) throws IOException {
        if (n < 0 || n > 32) {
            throw new IOException("n must be between 0 and 32");
        }

        int result = 0;
        int collected = 0;

        for (Bits current : buffer) {
            if (collected == n) break;

            int take = Math.min(n - collected, current.len);

            int extracted = BitUtils.right(current.pattern, take);

            result |= extracted << collected;
            collected += take;
        }

        if (collected < n) {
            throw new IOException("Not enough bits in BitQueue");
        }

        return result;
    }

    public void lock() {
        backupLen = remainingLen;
        remainingLen = 0;
    }

    public void unlock() {
        remainingLen = backupLen;
    }

    public int getRemainingLen() {
        return remainingLen;
    }

    private static class Bits {
        private int pattern;
        private int len;

        public Bits(int pattern, int len) {
            this.pattern = pattern;
            this.len = len;
        }

        private int extract(int n) {
            int amt = Math.min(n, len);
            int extracted = BitUtils.right(pattern, amt);
            pattern >>>= amt;
            len -= amt;
            return extracted;
        }
    }
}

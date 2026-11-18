package com.vke.core.file.deflate.decompress.lz77;

import com.vke.core.file.deflate.decompress.check.Checksum32;
import com.vke.utils.IntUtils;
import org.jetbrains.annotations.Nullable;

public class SlidingWindow {
    private final byte[] buffer;
    private int seam;
    private int bufferOccupied;
    private int mask;

    public SlidingWindow(int byteSize) {
        if (!IntUtils.isPowerOf2(byteSize)) {
            throw new IllegalArgumentException("SlidingWindow size MUST be a power of 2!");
        }
        this.buffer = new byte[byteSize];
        this.mask = byteSize - 1;
    }

    public final void append(byte b) {
        buffer[seam] = b;
        seam = (seam + 1) & mask;
        bufferOccupied = Math.min(bufferOccupied + 1, buffer.length);
    }

    public final byte backtrack(int distance) {
        if (distance <= 0 || distance > bufferOccupied) {
            //silent fail cuz this should never happen
            return 0;
        }

        //-1 cuz the seam has been incremented already
        int index = seam - distance;
        if (index < 0) {
            index += buffer.length;
        }
        return buffer[index];
    }

    public final void fastBacktrackAndAppend(byte[] dst, int start, int length, int distance, @Nullable Checksum32 checksum) {
        int readIndex = (seam - distance) & mask;
        int writeIndex = seam;

        if (checksum == null) {
            for (int i = 0; i < length; i++) {
                byte b = buffer[readIndex];
                buffer[writeIndex] = b;
                dst[start + i] = b;

                readIndex = (readIndex + 1) & mask;
                writeIndex = (writeIndex + 1) & mask;
            }
        }
        else {
            for (int i = 0; i < length; i++) {
                byte b = buffer[readIndex];
                buffer[writeIndex] = b;
                dst[start + i] = b;
                //i have this as a separate for loop so the jit can do stuff here
                checksum.nextByte(b);

                readIndex = (readIndex + 1) & mask;
                writeIndex = (writeIndex + 1) & mask;
            }
        }

        seam = writeIndex;
        bufferOccupied = Math.min(bufferOccupied + length, buffer.length);
    }
}

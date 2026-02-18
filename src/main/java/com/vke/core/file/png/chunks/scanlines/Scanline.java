package com.vke.core.file.png.chunks.scanlines;

import com.vke.core.file.png.PngInfo;
import com.vke.core.file.zlib.ZlibDecompressor;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class Scanline {
    private final @Nullable Scanline previous;
    private final FilterMethod filterMethod;
    private final byte[] filteredBytes;
    public byte[] unfilteredBytes;
    private final int pixelStride;

    public Scanline(@Nullable Scanline previous, PngInfo pngInfo, ZlibDecompressor data) throws IOException {
        this(previous, pngInfo, data, pngInfo.width);
    }

    public Scanline(@Nullable Scanline previous, PngInfo pngInfo, ZlibDecompressor data, int width) throws IOException {
        this.previous = previous;
        this.pixelStride = pngInfo.getPixelStride();
        this.filteredBytes = new byte[pixelStride * width];
        this.unfilteredBytes = new byte[pixelStride * width];

        int filterByte = data.nextByte();
        FilterMethod[] availableFilters = FilterMethod.values();
        if (filterByte < 0 || filterByte >= availableFilters.length) {
            throw new IOException("Illegal Filter byte found at scanline: " + filterByte);
        }
        this.filterMethod = availableFilters[filterByte];

        for (int i = 0; i < filteredBytes.length; i++) {
            int next = data.nextByte();
            if (next < 0) {
                throw new IOException("Unexpected EOF!");
            }
            filteredBytes[i] = (byte) next;
        }

        unfilter();
    }

    private void unfilter() {
        switch (filterMethod) {
            case None -> unfilteredBytes = filteredBytes;
            case Sub -> filterSub();
            case Up -> filterUp();
            case Average -> filterAverage();
            case Paeth -> filterPaeth();
        }
    }

    private short getFiltered(int index, int dist) {
        return getFiltered(this, index, dist);
    }

    private short getFiltered(Scanline line, int index, int dist) {
        if (line == null) return 0;
        int idx = index + dist * pixelStride;
        if (idx < 0) return 0;
        if (idx >= line.filteredBytes.length) return 0;
        return (short) (line.filteredBytes[idx] & 0xFF);
    }

    private short getUnfiltered(int index, int dist) {
        return getUnfiltered(this, index, dist);
    }

    private short getUnfiltered(Scanline line, int index, int dist) {
        if (line == null) return 0;
        int idx = index + dist * pixelStride;
        if (idx < 0) return 0;
        if (idx >= line.unfilteredBytes.length) return 0;
        return (short) (line.unfilteredBytes[idx] & 0xFF);
    }

    private void filterSub() {
        for (int i = 0; i < filteredBytes.length; i++) {
            short filtered = (short) (filteredBytes[i] & 0xFF);
            short unfiltered = (short) (filtered + getUnfiltered(i, -1));
            unfiltered %= 256;
            unfilteredBytes[i] = (byte) unfiltered;
        }
    }

    private void filterUp() {
        for (int i = 0; i < filteredBytes.length; i++) {
            short filtered = (short) (filteredBytes[i] & 0xFF);
            short unfiltered = (short) (filtered + getUnfiltered(previous, i, 0));
            unfiltered %= 256;
            unfilteredBytes[i] = (byte) unfiltered;
        }
    }

    private void filterAverage() {
        for (int i = 0; i < filteredBytes.length; i++) {
            short left = getUnfiltered(i, -1);
            short up = getUnfiltered(previous, i, 0);
            short avg = (short) ((left + up) / 2);
            short filtered = (short) (filteredBytes[i] & 0xFF);
            unfilteredBytes[i] = (byte) ((filtered + avg) & 0xFF);
        }
    }

    private void filterPaeth() {
        for (int i = 0; i < filteredBytes.length; i++) {
            short paeth = getFiltered(i, 0);
            short a = getUnfiltered(i, -1);
            short b = getUnfiltered(previous, i, 0);
            short c = getUnfiltered(previous, i, -1);
            short pred = predictor(a, b, c);
            unfilteredBytes[i] = (byte) ((pred + paeth) & 0xFF);
        }
    }

    private short predictor(short a, short b, short c) {
        int p = a + b - c;

        int pa = Math.abs(p - a);
        int pb = Math.abs(p - b);
        int pc = Math.abs(p - c);

        if (pa <= pb && pa <= pc) {
            return a;
        } else if (pb <= pc) {
            return b;
        } else {
            return c;
        }
    }
}

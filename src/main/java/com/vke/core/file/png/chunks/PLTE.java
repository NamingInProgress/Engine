package com.vke.core.file.png.chunks;

import java.io.IOException;
import java.io.InputStream;

public class PLTE extends PngChunk {
    private int[][] palette;

    @Override
    protected void readContents(InputStream stream) throws IOException {
        if (dataLength % 3 != 0) {
            throw new IOException("Chunk data length is not divisible by 3!");
        }

        int entries = dataLength / 3;

        if (entries > 256) {
            throw new IOException("Palette cannot contain more than 256 entries");
        }

        palette = new int[entries][3];
        for (int i = 0; i < entries; i++) {
            int r = readInt8(stream);
            int g = readInt8(stream);
            int b = readInt8(stream);
            palette[i][0] = r;
            palette[i][1] = g;
            palette[i][2] = b;
        }
    }

    public int[][] getPalette() {
        return palette;
    }
}

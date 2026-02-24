package com.vke.core.file.png.chunks;

import java.io.IOException;
import java.io.InputStream;

public class IEND extends PngChunk {
    @Override
    protected void readContents(InputStream stream) throws IOException {
        if (dataLength != 0) {
            throw new IOException("IEND chunks cannot have any data assigned to them!");
        }
    }
}

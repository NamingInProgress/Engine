package com.vke.core.file.png.chunks;

import java.io.IOException;
import java.io.InputStream;

public class UnknownChunk extends PngChunk {
    @Override
    protected void readContents(InputStream stream) throws IOException {
        for (int i = 0; i < dataLength; i++) readInt8(stream);
    }
}
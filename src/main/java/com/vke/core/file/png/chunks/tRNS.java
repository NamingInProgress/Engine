package com.vke.core.file.png.chunks;

import com.vke.core.file.io.bit.BitInputStream;

import java.io.IOException;
import java.io.InputStream;

public class tRNS extends PngChunk {
    @Override
    protected void readContents(InputStream stream) throws IOException {
        throw new IOException("tRNS chunks are not supported rn");
    }
}

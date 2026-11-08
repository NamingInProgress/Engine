package com.vke.core.file.exr.decompress;

import java.io.IOException;
import java.io.InputStream;

public interface ExrDecompressor {
    void decompress(InputStream stream, byte[] dst, int start, int length) throws IOException;
}

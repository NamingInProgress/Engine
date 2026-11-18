package com.vke.core.file.exr.decompress;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class ExrNoneDecompressor implements ExrDecompressor{
    @Override
    public void decompress(InputStream stream, byte[] dst, int start, int length) throws IOException {
        int read = 0;
        while (read < length) {
            int n = stream.read(dst, start + read, length - read);
            if (n == -1) {
                throw new EOFException("EOF when trying to read more data!");
            }
            read += n;
        }
    }
}

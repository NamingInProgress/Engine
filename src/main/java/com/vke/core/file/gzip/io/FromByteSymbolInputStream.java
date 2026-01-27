package com.vke.core.file.gzip.io;

import java.io.IOException;
import java.io.InputStream;

public class FromByteSymbolInputStream extends SymbolInputStream {
    private final InputStream stream;

    public FromByteSymbolInputStream(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public int readNextSymbol() throws IOException {
        return stream.read();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}

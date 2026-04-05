package com.vke.core.vz.chunks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface VzChunk {
    void write(OutputStream stream) throws IOException;

    interface Factory<C extends VzChunk> {
        C read(InputStream stream) throws IOException;
    }
}

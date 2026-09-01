package com.vke.core.serializer.impl.load;

import com.vke.api.serializer.Loader;
import com.vke.core.serializer.LoadException;
import com.vke.utils.Utils;

import java.io.IOException;
import java.io.InputStream;

public class BinaryLoader implements Loader {
    private final InputStream stream;
    private boolean closed = false;

    public BinaryLoader(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public byte loadByte() throws LoadException {
        if (closed) {
            throw new LoadException("Stream closed manually prior to read call!");
        }
        return Utils.chainExceptions(() -> {
            int next = stream.read();
            if (next < 0) {
                throw new LoadException(
                        "Tried to load byte from BinaryLoader, but the data is insufficient!"
                );
            }
            return (byte) next;
        });
    }

    public void close() throws IOException {
        stream.close();
        this.closed = true;
    }
}
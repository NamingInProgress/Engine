package com.vke.core.serializer.impl.load;

import com.vke.api.serializer.Loader;
import com.vke.core.serializer.LoadException;
import com.vke.utils.Utils;

import java.io.IOException;
import java.io.InputStream;

public class BinaryLoader implements Loader {
    private final InputStream stream;
    private boolean closed = false;

    public static final int BUFFER_SIZE = 4096 * 4;
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private int bufferLength, bufferPointer;
    private boolean inputExhausted;

    public BinaryLoader(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public byte loadByte() throws LoadException {
        if (closed) {
            throw new LoadException("Stream closed manually prior to read call!");
        }

        try {
            if (bufferPointer >= bufferLength) {
                if (inputExhausted) {
                    throw new LoadException("No more bytes available!");
                }
                bufferLength = stream.readNBytes(buffer, 0, BUFFER_SIZE);
                if (bufferLength < BUFFER_SIZE) {
                    inputExhausted = true;
                }
                bufferPointer = 0;
            }
            return buffer[bufferPointer++];
        } catch (IOException e) {
            throw new LoadException(e);
        }
    }

    public void close() throws IOException {
        stream.close();
        this.closed = true;
    }
}
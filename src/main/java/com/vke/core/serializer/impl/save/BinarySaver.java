package com.vke.core.serializer.impl.save;

import com.vke.api.serializer.Saver;
import com.vke.core.serializer.SaveException;
import com.vke.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BinarySaver implements Saver {
    private final OutputStream out;

    public static final int BUFFER_SIZE = 4096 * 4;
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private int bufferPointer;

    public BinarySaver(OutputStream out) {
        this.out = out;
    }

    @Override
    public void saveByte(byte v) throws SaveException {
        try {
            buffer[bufferPointer++] = v;
            if (bufferPointer >= BUFFER_SIZE) {
                out.write(buffer, 0, BUFFER_SIZE);
                bufferPointer = 0;
            }
        } catch (IOException e) {
            throw new SaveException(e);
        }
    }

    @Override
    public void flush() throws IOException {
        out.write(buffer, 0, bufferPointer);
    }
}
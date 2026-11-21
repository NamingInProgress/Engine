package com.vke.core.serializer;

import com.vke.api.serializer.Saver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ByteSaver implements Saver {
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Override
    public void saveByte(byte v) {
        out.write(v);
    }

    @Override
    public void flush() throws IOException {

    }

    public byte[] asArray() {
        return out.toByteArray();
    }
}
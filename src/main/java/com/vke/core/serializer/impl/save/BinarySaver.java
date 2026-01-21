package com.vke.core.serializer.impl.save;

import com.vke.api.serializer.Saver;

import java.io.ByteArrayOutputStream;

public class BinarySaver implements Saver {
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Override
    public void saveByte(byte v) {
        out.write(v);
    }

    public byte[] asArray() {
        return out.toByteArray();
    }
}
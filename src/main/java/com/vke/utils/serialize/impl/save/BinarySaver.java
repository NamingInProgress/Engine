package com.vke.utils.serialize.impl.save;

import com.vke.utils.serialize.Saver;

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
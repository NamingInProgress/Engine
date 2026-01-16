package com.vke.utils.serialize;

import java.io.ByteArrayOutputStream;

public class ByteSaver implements Saver {
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Override
    public void saveByte(byte v) {
        out.write(v);
    }

    public byte[] asArray() {
        return out.toByteArray();
    }
}
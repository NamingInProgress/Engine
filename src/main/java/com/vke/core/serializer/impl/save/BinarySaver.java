package com.vke.core.serializer.impl.save;

import com.vke.api.serializer.Saver;
import com.vke.core.serializer.SaveException;
import com.vke.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BinarySaver implements Saver {
    private final OutputStream out;

    public BinarySaver(OutputStream out) {
        this.out = out;
    }

    @Override
    public void saveByte(byte v) throws SaveException {
        try {
            out.write(v);
        } catch (IOException e) {
            throw new SaveException(e);
        }
    }
}
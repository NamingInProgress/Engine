package com.vke.core.serializer.impl.load;

import com.vke.api.serializer.Loader;
import com.vke.utils.exception.LoadException;

public class BinaryLoader implements Loader {
    private final byte[] data;
    private int pos = 0;

    public BinaryLoader(byte[] data) {
        this.data = data;
    }

    @Override
    public byte loadByte() throws LoadException {
        if (pos >= data.length) {
            throw new LoadException(
                    "Tried to load byte from BinaryLoader, but the data is insufficient!"
            );
        }
        return data[pos++];
    }
}
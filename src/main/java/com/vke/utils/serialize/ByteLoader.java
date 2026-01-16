package com.vke.utils.serialize;

import com.vke.utils.serialize.exec.LoadException;

public class ByteLoader implements Loader {
    private final byte[] data;
    private int pos = 0;

    public ByteLoader(byte[] data) {
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
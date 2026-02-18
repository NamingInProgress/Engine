package com.vke.core.file.riff;

public class RIFFBinaryPayload extends RIFFPayload {
    private final byte[] data;

    public RIFFBinaryPayload(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}

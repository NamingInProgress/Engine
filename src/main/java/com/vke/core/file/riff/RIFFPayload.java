package com.vke.core.file.riff;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class RIFFPayload {
    public static RIFFPayload readBinaryPayload(int length, InputStream stream) throws IOException {
        byte[] data = stream.readNBytes(length);
        if (data.length != length) {
            throw new EOFException("Insufficient amounts of bytes read! Expected " + length + ", read only " + data.length);
        }
        return new RIFFBinaryPayload(data);
    }
}

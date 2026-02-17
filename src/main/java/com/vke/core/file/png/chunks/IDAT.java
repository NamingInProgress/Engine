package com.vke.core.file.png.chunks;

import java.io.IOException;
import java.io.InputStream;

public class IDAT extends PngChunk {
    private byte[] zlibData;

    @Override
    protected void readContents(InputStream stream) throws IOException {
        zlibData = new byte[dataLength];
        for (int i = 0; i < dataLength; i++) {
            byte b = (byte) stream.read();
            checksum.nextByte(b);
            zlibData[i] = b;
        }
    }

    public byte[] getZlibData() {
        return zlibData;
    }
}

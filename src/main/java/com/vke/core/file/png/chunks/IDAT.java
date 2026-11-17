package com.vke.core.file.png.chunks;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class IDAT extends PngChunk {
    private byte[] zlibData;

    @Override
    protected void readContents(InputStream stream) throws IOException {
        zlibData = new byte[dataLength];
        for (int n = 0; n < dataLength;) {
            int r = stream.read(zlibData, n, dataLength - n);
            if (r == -1) {
                throw new EOFException("EOF");
            }
            n += r;
        }
        for (int i = 0; i < dataLength; i++) {
            //fast cache friendly loop hopefully
            checksum.nextByte(zlibData[i]);
        }
    }

    public byte[] getZlibData() {
        return zlibData;
    }
}

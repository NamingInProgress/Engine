package com.vke.core.file.exr.decompress;

import com.vke.api.file.ByteDecoder;
import com.vke.api.file.DecodeException;
import com.vke.api.file.Decoders;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class ExrZipDecompressor implements ExrDecompressor {
    @Override
    public void decompress(InputStream stream, byte[] dst, int start, int length) throws IOException {
        try {
            ByteDecoder zlib = Decoders.make("zlib");
            if (zlib == null) throw new IOException("No zlib decoder found");
            zlib.setSource(stream);

            byte[] tmp = new byte[length];
            for (int n = 0; n < length;) {
                int read = zlib.decode(tmp, n, length - n);
                if (read == -1) {
                    throw new EOFException(String.format("Inflating stopped too early! (%d out of %d read)", n + 1, length));
                }
                n += read;
            }
            int half = (length + 1) / 2;
            byte last = tmp[0];
            dst[0] = last;
            for (int i = 1; i < length; i++) {
                int d = tmp[i] + last - 128;
                byte b = (byte) (d & 0xFF);
                last = b;
                if (i < half) {
                    dst[start + i * 2] = b;
                } else {
                    dst[start + (i - half) * 2 + 1] = b;
                }
            }
        } catch (DecodeException e) {
            throw new IOException("Decoding error", e);
        }
    }
}

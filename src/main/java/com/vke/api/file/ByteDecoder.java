package com.vke.api.file;

import com.vke.core.Context;
import com.vke.core.FileIdentifier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

public interface ByteDecoder extends Decoder<byte[]> {
    default void setSource(byte[] src) {
        setSource(src, 0, src.length);
    }

    default void setSource(byte[] src, int start, int length) {
        setSource(new ByteArrayInputStream(src, start, length));
    }

    void setSource(InputStream stream);

    default int decode(byte[] dst) throws DecodeException {
        return decode(dst, 0, dst.length);
    }

    /// returns -1 when done
    int decode(byte[] dst, int start, int length) throws DecodeException;

    @Override
    default byte[] decode(Context context, InputStream input) throws DecodeException {
        setSource(input);
        final int BUF_SIZE = 4096;
        int bytesRead = 0;
        byte[] buffer = new byte[BUF_SIZE];
        while (true) {
            int read = decode(buffer, bytesRead, BUF_SIZE);
            if (read == -1) {
                buffer = Arrays.copyOf(buffer, bytesRead);
                break;
            }
            bytesRead += read;
            buffer = Arrays.copyOf(buffer, buffer.length + BUF_SIZE);
        }

        return buffer;
    }
}

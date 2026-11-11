package com.vke.core.file.exr.decompress;

import com.vke.api.file.DecodeException;
import com.vke.api.file.Decoders;
import com.vke.api.file.LazyArray;
import com.vke.api.file.LazyDecoder;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class ExrZipDecompressor implements ExrDecompressor {
    @Override
    public void decompress(InputStream stream, byte[] dst, int start, int length) throws IOException {
        try {
            LazyDecoder<Integer> decoder = Decoders.find("zlib");

            if (decoder == null) throw new IOException("No zlib decoder found");
            LazyArray<Integer> arr = decoder.decodeLazy(stream);

            for (int i = 0; i < length; i++) {
                Integer b = arr.next();
                if (b == null || b == -1) {
                    throw new EOFException(String.format("Inflating stopped too early! (%d out of %d read)", i + 1, length));
                }
                if (i == 0) {
                    dst[start] = (byte) (b & 0xFF);
                } else {
                    int last = dst[start + i - 1];
                    int d = b + last - 128;
                    dst[start + i] = (byte) (d & 0xFF);
                }
            }
            interleave(dst, start, length);
        } catch (DecodeException e) {
            throw new IOException("Decoding error: " + e.getMessage());
        }
    }

    private static void interleave(byte[] dst, int start, int length) {
        byte[] tmp = new byte[length];
        System.arraycopy(dst, start, tmp, 0, length);

        int firstHalf = 0;
        int secondHalf = (length + 1) / 2;
        int out = start;
        int end = start + length;

        while (out < end) {
            dst[out++] = tmp[firstHalf++];
            if (out < end) {
                dst[out++] = tmp[secondHalf++];
            }
        }
    }
}

package com.vke.core.file.decoders;

import com.vke.api.file.ByteDecoder;
import com.vke.api.file.DecodeException;
import com.vke.core.file.gzip.GzipDecompressor;
import com.vke.core.file.io.bit.input.FastBitInputStream;

import java.io.IOException;
import java.io.InputStream;

public class GzipDecoder implements ByteDecoder {
    public static final String KEY = "gzip";

    private GzipDecompressor gzip;

    @Override
    public void setSource(InputStream stream) {
        this.gzip = new GzipDecompressor(FastBitInputStream.withLimitRespect(stream));
    }

    @Override
    public int decode(byte[] dst, int start, int length) throws DecodeException {
        try {
            return gzip.decompress(dst, start, length);
        } catch (IOException e) {
            throw DecodeException.ofIO(e);
        }
    }
}

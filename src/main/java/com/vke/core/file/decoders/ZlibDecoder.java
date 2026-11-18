package com.vke.core.file.decoders;

import com.vke.api.file.ByteDecoder;
import com.vke.api.file.DecodeException;
import com.vke.core.file.io.bit.input.FastBitInputStream;
import com.vke.core.file.zlib.ZlibDecompressor;
import com.vke.utils.io.LimitedInputStream;

import java.io.IOException;
import java.io.InputStream;

public class ZlibDecoder implements ByteDecoder {
    public static final String KEY = "zlib";
    private ZlibDecompressor zlib;

    @Override
    public void setSource(InputStream stream) {
        this.zlib = new ZlibDecompressor(FastBitInputStream.withLimitRespect(stream));
    }

    @Override
    public int decode(byte[] dst, int start, int length) throws DecodeException {
        try {
            return zlib.decompress(dst, start, length);
        } catch (IOException e) {
            throw DecodeException.ofIO(e);
        }
    }
}

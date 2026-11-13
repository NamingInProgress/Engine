package com.vke.core.file.decoders;

import com.vke.api.file.ByteDecoder;
import com.vke.api.file.DecodeException;

import java.io.InputStream;

public class GzipDecoder implements ByteDecoder {
    public static final String KEY = "gzip";

    @Override
    public void setSource(InputStream stream) {

    }

    @Override
    public int decode(byte[] dst, int start, int length) throws DecodeException {
        return 0;
    }
}

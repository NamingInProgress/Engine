package com.vke.core.file.exr.extract;

import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.head.ExrCompression;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public abstract class ExrExtractor {
    protected final ExrDecoder decoder;
    protected final InputStream stream;

    protected ExrExtractor(ExrDecoder decoder, InputStream stream) {
        this.decoder = decoder;
        this.stream = stream;
    }

    protected long nextChunk() throws IOException {
        if (decoder.head.isMultipart) {
            return DataUtils.readU64LittleEndian(stream);
        } else {
            return 0;
        }
    }

    protected byte[] decompress(int length, ExrCompression cmpr) {
        byte[] dst = new byte[length];
        decompress(dst, 0, length, cmpr);
        return dst;
    }

    protected void decompress(byte[] dst, ExrCompression cmpr) {
        decompress(dst, 0, dst.length, cmpr);
    }

    protected void decompress(byte[] dst, int start, int length, ExrCompression cmpr) {

    }
}

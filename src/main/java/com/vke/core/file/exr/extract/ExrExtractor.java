package com.vke.core.file.exr.extract;

import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.decompress.ExrDecompressor;
import com.vke.core.file.exr.head.ExrCompression;
import com.vke.core.file.utils.DataUtils;
import com.vke.utils.io.LimitedInputStream;
import com.vke.utils.io.PositionedInputStream;

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

    protected byte[] decompress(int length, ExrCompression cmpr, int streamLimit) throws IOException {
        byte[] dst = new byte[length];
        decompress(dst, 0, length, cmpr, streamLimit);
        return dst;
    }

    protected void decompress(byte[] dst, ExrCompression cmpr, int streamLimit) throws IOException {
        decompress(dst, 0, dst.length, cmpr, streamLimit);
    }

    protected long decompress(byte[] dst, int start, int length, ExrCompression cmpr, int streamLimit) throws IOException {
        ExrDecompressor decomp = decoder.getDecompressor(cmpr);
        if (decomp == null) {
            throw new IOException("No decompressor found for compression method " + cmpr);
        }
        PositionedInputStream pStream = new PositionedInputStream(stream);
        LimitedInputStream limited = new LimitedInputStream(pStream, streamLimit);
        decomp.decompress(limited, dst, start, length);
        return pStream.getPosition();
    }
}

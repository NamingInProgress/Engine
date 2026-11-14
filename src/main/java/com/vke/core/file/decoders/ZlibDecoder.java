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
    private boolean headerParsed;
    private boolean finished;

    @Override
    public void setSource(InputStream stream) {
        if (stream instanceof LimitedInputStream limited) {
            this.zlib = new ZlibDecompressor(new FastBitInputStream(stream, limited.getLimit()));
        } else {
            this.zlib = new ZlibDecompressor(new FastBitInputStream(stream));
        }
        this.headerParsed = false;
        this.finished = false;
    }

    @Override
    public int decode(byte[] dst, int start, int length) throws DecodeException {
        if (finished) {
            return -1;
        }
        if (zlib == null) {
            throw new DecodeException("No source has been set yet! Please set a source first before decoding can start...");
        }
        try {
            if (!headerParsed) {
                zlib.parseHeader();
                headerParsed = true;
            }
            int read = zlib.decompressBytes(dst, start, length);
            if (read == -1) {
                zlib.parseFooter();
                finished = true;
                return read;
            }
            return read;
        } catch (IOException e) {
            DecodeException ex = new DecodeException(e.getMessage());
            ex.setStackTrace(e.getStackTrace());
            throw ex;
        }
    }
}

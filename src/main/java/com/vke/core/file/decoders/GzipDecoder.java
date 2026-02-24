package com.vke.core.file.decoders;

import com.carrotsearch.hppc.ByteArrayList;
import com.vke.core.file.gzip.GzipDecompressor;
import com.vke.core.file.io.bit.input.ShittyBitInputStream;
import com.vke.core.file.utils.HBFLazyDecoder;

import java.io.InputStream;

public class GzipDecoder extends HBFLazyDecoder<GzipDecompressor, Integer, byte[]> {
    public static final String KEY = "gzip";
    private ByteArrayList collection;

    @Override
    protected GzipDecompressor fromStream(InputStream stream) {
        return new GzipDecompressor(new ShittyBitInputStream(stream));
    }

    @Override
    protected boolean isFinal(Integer element) {
        return element == -1;
    }

    @Override
    protected void startCollecting() {
        collection = new ByteArrayList();
    }

    @Override
    protected void addNext(Integer element) {
        collection.add(element.byteValue());
    }

    @Override
    protected byte[] combine() {
        return collection.toArray();
    }
}

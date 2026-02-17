package com.vke.core.file.decoders;

import com.carrotsearch.hppc.ByteArrayList;
import com.vke.core.file.io.bit.ShittyBitInputStream;
import com.vke.core.file.utils.HBFLazyDecoder;
import com.vke.core.file.zlib.ZlibDecompressor;

import java.io.InputStream;

public class ZlibDecoder extends HBFLazyDecoder<ZlibDecompressor, Integer, byte[]> {
    public static final String KEY = "zlib";
    private ByteArrayList collection;

    @Override
    protected ZlibDecompressor fromStream(InputStream stream) {
        return new ZlibDecompressor(new ShittyBitInputStream(stream));
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

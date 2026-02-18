package com.vke.core.file.riff;

import com.vke.api.file.Decoder;
import com.vke.core.file.riff.chunks.RIFFListChunk;
import com.vke.core.file.riff.chunks.RIFFRootChunk;

public abstract class RIFFDecoder<T> implements Decoder<T> {
    protected final RIFFFormat format;

    public RIFFDecoder(RIFFFormat format) {
        this.format = format;

        format.registerChunk(RIFFRootChunk.NAME, new RIFFRootChunk.Factory());
        format.registerChunk(RIFFListChunk.NAME, new RIFFListChunk.Factory());

        format.registerChunks();

    }
}

package com.vke.core.file.riff;

import com.vke.core.file.utils.Ascii4;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public abstract class RIFFFormat {
    private final HashMap<Ascii4, RIFFChunkFactory> factories = new HashMap<>();

    protected abstract void registerChunks();

    protected void registerChunk(Ascii4 name, RIFFChunkFactory factory) {
        factories.put(name, factory);
    }

    public RIFFChunkFactory findFactory(Ascii4 name) {
        return factories.get(name);
    }

    public RIFFChunk readNextChunk(InputStream stream) throws IOException {
        Ascii4 name = Ascii4.read(stream);
        RIFFChunkFactory factory = findFactory(name);
        if (factory == null) {
            throw new IOException("Unknown RIFF Chunk: " + name);
        }
        return factory.readChunk(name, stream, this);
    }
}

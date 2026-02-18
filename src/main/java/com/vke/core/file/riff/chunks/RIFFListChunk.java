package com.vke.core.file.riff.chunks;

import com.vke.core.file.riff.RIFFChunk;
import com.vke.core.file.riff.RIFFChunkFactory;
import com.vke.core.file.riff.RIFFFormat;
import com.vke.core.file.utils.Ascii4;

import java.io.IOException;
import java.io.InputStream;

public class RIFFListChunk extends RIFFContainerChunk {
    public static final Ascii4 NAME = Ascii4.of("LIST");

    public RIFFListChunk(Ascii4 name, InputStream stream, RIFFFormat format) throws IOException {
        super(name, stream, format);
    }

    public static class Factory implements RIFFChunkFactory {
        @Override
        public RIFFChunk readChunk(Ascii4 name, InputStream stream, RIFFFormat format) throws IOException {
            return new RIFFListChunk(name, stream, format);
        }
    }
}

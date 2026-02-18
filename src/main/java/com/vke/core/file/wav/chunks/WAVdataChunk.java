package com.vke.core.file.wav.chunks;

import com.vke.core.file.riff.RIFFChunk;
import com.vke.core.file.riff.RIFFChunkFactory;
import com.vke.core.file.riff.RIFFFormat;
import com.vke.core.file.riff.chunks.RIFFBinaryChunk;
import com.vke.core.file.utils.Ascii4;

import java.io.IOException;
import java.io.InputStream;

public class WAVdataChunk extends RIFFBinaryChunk {
    public static final Ascii4 NAME = Ascii4.of("data");

    public WAVdataChunk(Ascii4 name, InputStream stream, RIFFFormat format) throws IOException {
        super(name, stream, format);
    }

    public static class Factory implements RIFFChunkFactory {
        @Override
        public RIFFChunk readChunk(Ascii4 name, InputStream stream, RIFFFormat format) throws IOException {
            return new WAVdataChunk(name, stream, format);
        }
    }
}

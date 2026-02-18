package com.vke.core.file.riff;

import com.vke.core.file.utils.Ascii4;

import java.io.IOException;
import java.io.InputStream;

public interface RIFFChunkFactory {
    RIFFChunk readChunk(Ascii4 name, InputStream stream, RIFFFormat format) throws IOException;
}

package com.vke.core.file.riff.chunks;

import com.vke.core.file.riff.RIFFChunk;
import com.vke.core.file.riff.RIFFFormat;
import com.vke.core.file.riff.RIFFPayload;
import com.vke.core.file.utils.Ascii4;

import java.io.IOException;
import java.io.InputStream;

public class RIFFBinaryChunk extends RIFFChunk {
    public RIFFBinaryChunk(Ascii4 name, InputStream stream, RIFFFormat format) throws IOException {
        super(name, stream, format);
    }

    @Override
    protected RIFFPayload readPayload(InputStream stream, RIFFFormat format) throws IOException {
        return RIFFPayload.readBinaryPayload((int) size, stream);
    }
}

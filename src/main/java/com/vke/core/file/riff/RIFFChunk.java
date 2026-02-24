package com.vke.core.file.riff;

import com.vke.core.file.utils.Ascii4;
import com.vke.core.file.utils.DataUtils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public abstract class RIFFChunk extends RIFFPayload {
    protected final Ascii4 name;
    protected final long size;
    protected final long actualSize;
    protected final RIFFPayload payload;

    public RIFFChunk(Ascii4 name, InputStream stream, RIFFFormat format) throws IOException {
        this.name = name;
        int size = DataUtils.readU32LittleEndian(stream);
        this.size = DataUtils.unsign32(size);
        this.payload = readPayload(stream, format);
        if (size % 2 == 1) {
            //read once extra byte here because the chunks are aligned to even numbers only for some reason
            int pad = stream.read();
            if (pad == -1) {
                throw new EOFException("Missing RIFF padding byte");
            }
            this.actualSize = size + 1;
        } else {
            this.actualSize = size;
        }
    }

    protected abstract RIFFPayload readPayload(InputStream stream, RIFFFormat format) throws IOException;

    public long size() {
        return size;
    }

    public long actualSize() {
        return actualSize;
    }

    public Ascii4 name() {
        return name;
    }

    public RIFFPayload payload() {
        return payload;
    }
}

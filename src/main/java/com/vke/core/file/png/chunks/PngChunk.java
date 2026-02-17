package com.vke.core.file.png.chunks;

import com.vke.core.file.io.bit.BitInputStream;
import com.vke.core.file.io.bit.BitOrdering;
import com.vke.core.file.io.bit.BitStreamUtils;

import java.io.IOException;

public abstract class PngChunk {
    protected int dataLength;
    protected int type;
    protected int crc;

    public void readChunkData(BitInputStream stream) throws IOException {
        stream.setOrdering(BitOrdering.LSB_FIRST);
        dataLength = BitStreamUtils.readBigEndian32(stream);
        readContents(stream);
    }

    protected abstract void readContents(BitInputStream stream);
}

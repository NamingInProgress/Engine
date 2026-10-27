package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class BytesAttr implements ExrAttr {
    public final int hintLength;
    public final @Nullable String typeHint;
    public final byte[] payload;

    public BytesAttr(InputStream stream, int size) throws IOException {
        this.hintLength = DataUtils.readU32LittleEndian(stream);
        int pos = 4;
        if (hintLength != 0) {
            byte[] charData = stream.readNBytes(hintLength);
            pos += hintLength;
            this.typeHint = new String(charData, StandardCharsets.US_ASCII);
        } else {
            this.typeHint = null;
        }
        this.payload = stream.readNBytes(size - pos);
    }
}

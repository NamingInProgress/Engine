package com.vke.core.file.exr.head;

import com.vke.core.file.utils.Ascii4;
import com.vke.core.file.utils.BitPackerUtils;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class ExrHeaderHead {
    private static final Ascii4 MAGIC = Ascii4.ofBytes(0x76, 0x2f, 0x31, 0x01);

    public final Ascii4 magic;
    public final int version;
    public final boolean isSingleTile;
    public final boolean hasLongNames;
    public final boolean hasDeepData;
    public final boolean isMultipart;

    public ExrHeaderHead(InputStream stream) throws IOException {
        DataUtils.enableThrowOnEOF();
        this.magic = Ascii4.read(stream);
        if (!magic.equals(MAGIC)) throw new IOException("The provided InputStream is NOT a valid exr stream, magic bytes mismatch!");
        this.version = DataUtils.readU8(stream);
        if (version != 2) throw new IOException("Currently only exr 2.0 files are supported.");
        int[] remaining = DataUtils.readU8N(3, stream);
        if (remaining[1] != 0 || remaining[2] != 0) throw new IOException("Version bits 13-31 must be 0 for an exr file to be valid!");
        boolean[] flags = BitPackerUtils.unpackU1(remaining[0], 4);
        this.isSingleTile = flags[0];
        this.hasLongNames = flags[1];
        this.hasDeepData = flags[2];
        this.isMultipart = flags[3];
        DataUtils.popConfig();
    }
}

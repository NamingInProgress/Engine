package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class KeycodeAttr implements ExrAttr {
    public final int filmMfcCode, filmType, prefix, count, perfOffset, perfsPerFrame, perfsPerCount;

    public KeycodeAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 4*7);
        this.filmMfcCode = DataUtils.readU32LittleEndian(stream);
        this.filmType = DataUtils.readU32LittleEndian(stream);
        this.prefix = DataUtils.readU32LittleEndian(stream);
        this.count = DataUtils.readU32LittleEndian(stream);
        this.perfOffset = DataUtils.readU32LittleEndian(stream);
        this.perfsPerFrame = DataUtils.readU32LittleEndian(stream);
        this.perfsPerCount = DataUtils.readU32LittleEndian(stream);
    }
}

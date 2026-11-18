package com.vke.core.file.exr;

import com.vke.core.file.exr.head.ExrHeader;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public record ExrPart(ExrHeader header, long[] offsetTable) {
    public ExrPart(ExrHeader header, InputStream stream) throws IOException {
        int offsetCount = header.computeOffsetCount();
        long[] offsetTable = new long[offsetCount];
        for (int i = 0; i < offsetCount; i++) {
            offsetTable[i] = DataUtils.readU64LittleEndian(stream);
        }
        //set your java to 25 bear
        this(header, offsetTable);
    }
}

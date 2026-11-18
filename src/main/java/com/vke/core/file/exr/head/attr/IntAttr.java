package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class IntAttr implements ExrAttr {
    public final int value;

    public IntAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 4);
        this.value = DataUtils.readU32LittleEndian(stream);
    }
}

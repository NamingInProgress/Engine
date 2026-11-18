package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class RationalAttr implements ExrAttr {
    public final int top, bottom;

    public RationalAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 4*2);
        top = DataUtils.readU32LittleEndian(stream);
        bottom = DataUtils.readU32LittleEndian(stream);
    }
}

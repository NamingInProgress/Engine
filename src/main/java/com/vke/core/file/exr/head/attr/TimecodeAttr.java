package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class TimecodeAttr implements ExrAttr {
    public final int timeAndFlags, userData;

    public TimecodeAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 4*2);
        this.timeAndFlags = DataUtils.readU32LittleEndian(stream);
        this.userData = DataUtils.readU32LittleEndian(stream);
    }
}

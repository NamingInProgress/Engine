package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class HalfAttr implements ExrAttr {
    public final float floatValue;
    public final short bits16;

    public HalfAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 2);
        this.bits16 = (short) DataUtils.readU16LittleEndian(stream);
        this.floatValue = Float.float16ToFloat(bits16);
    }
}

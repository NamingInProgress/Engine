package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class FloatAttr implements ExrAttr {
    public final float value;

    public FloatAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 4);
        int bits = DataUtils.readU32LittleEndian(stream);
        this.value = Float.intBitsToFloat(bits);
    }
}

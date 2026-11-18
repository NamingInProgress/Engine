package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class DoubleAttr implements ExrAttr {
    public final double value;

    public DoubleAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 8);
        long bits = DataUtils.readU64LittleEndian(stream);
        this.value = Double.longBitsToDouble(bits);
    }
}

package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class M44fAttr implements ExrAttr {
    public final float[] floats;

    public M44fAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 4*16);
        this.floats = new float[16];
        for (int i = 0; i < 16; i++) {
            int bits = DataUtils.readU32LittleEndian(stream);
            floats[i] = Float.intBitsToFloat(bits);
        }
    }
}

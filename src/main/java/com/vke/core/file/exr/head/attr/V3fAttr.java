package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class V3fAttr implements ExrAttr {
    public final float x, y, z;

    public V3fAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 4*3);
        this.x = f32(stream);
        this.y = f32(stream);
        this.z = f32(stream);
    }

    private static float f32(InputStream stream) throws IOException {
        int bits = DataUtils.readU32LittleEndian(stream);
        return Float.intBitsToFloat(bits);
    }
}

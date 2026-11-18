package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class V2fAttr implements ExrAttr {
    public final float x, y;

    public V2fAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 4*2);
        this.x = f32(stream);
        this.y = f32(stream);
    }

    private static float f32(InputStream stream) throws IOException {
        int bits = DataUtils.readU32LittleEndian(stream);
        return Float.intBitsToFloat(bits);
    }
}

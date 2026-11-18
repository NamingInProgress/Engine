package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class Box2fAttr implements ExrAttr {
    public final float xMin, yMin, xMax, yMax;

    public Box2fAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 4*4);
        this.xMin = f32(stream);
        this.yMin = f32(stream);
        this.xMax = f32(stream);
        this.yMax = f32(stream);
    }

    private static float f32(InputStream stream) throws IOException {
        int bits = DataUtils.readU32LittleEndian(stream);
        return Float.intBitsToFloat(bits);
    }
}

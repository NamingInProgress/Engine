package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class ChromaticitiesAttr implements ExrAttr {
    public final float redX, redY, greenX, greenY, blueX, blueY, whiteX, whiteY;

    public ChromaticitiesAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 4*8);
        this.redX = f32(stream);
        this.redY = f32(stream);
        this.greenX = f32(stream);
        this.greenY = f32(stream);
        this.blueX = f32(stream);
        this.blueY = f32(stream);
        this.whiteX = f32(stream);
        this.whiteY = f32(stream);
    }

    private static float f32(InputStream stream) throws IOException {
        int bits = DataUtils.readU32LittleEndian(stream);
        return Float.intBitsToFloat(bits);
    }
}

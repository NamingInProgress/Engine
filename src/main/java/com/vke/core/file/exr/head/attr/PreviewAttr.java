package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class PreviewAttr implements ExrAttr {
    public final int width, height;
    public final int[] pixels;

    public PreviewAttr(InputStream stream, int ignoreSize) throws IOException {
        this.width = DataUtils.readU32LittleEndian(stream);
        this.height = DataUtils.readU32LittleEndian(stream);
        this.pixels = new int[width * height];

        for (int i = 0; i < width * height; i++) {
            //lil optimiztaion here idk if thats correct but it will not corrupt later decoding :P
            pixels[i] = DataUtils.readU32LittleEndian(stream);
        }
    }
}

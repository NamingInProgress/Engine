package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class Box2iAttr implements ExrAttr {
    public final int xMin, yMin, xMax, yMax;

    public Box2iAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 4*4);
        this.xMin = DataUtils.readU32LittleEndian(stream);
        this.yMin = DataUtils.readU32LittleEndian(stream);
        this.xMax = DataUtils.readU32LittleEndian(stream);
        this.yMax = DataUtils.readU32LittleEndian(stream);
    }

    public int width() {
        return xMax - xMin + 1;
    }

    public int height() {
        return yMax - yMin + 1;
    }

    public boolean needsBg(Box2iAttr view) {
        return view.xMin < xMin || view.yMin < yMin || view.xMax > xMax || view.yMax > yMax;
    }
}

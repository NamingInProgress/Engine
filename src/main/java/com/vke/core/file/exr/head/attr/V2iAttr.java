package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class V2iAttr implements ExrAttr {
    public final int x, y;

    public V2iAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 4*2);
        this.x = DataUtils.readU32LittleEndian(stream);
        this.y = DataUtils.readU32LittleEndian(stream);
    }
}

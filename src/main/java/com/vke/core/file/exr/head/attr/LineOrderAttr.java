package com.vke.core.file.exr.head.attr;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import com.vke.core.file.exr.head.ExrLineOrder;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class LineOrderAttr implements ExrAttr {
    public final ExrLineOrder lineOrder;

    public LineOrderAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 1);
        int repr = DataUtils.readU8(stream);
        this.lineOrder = IntEnum.fromInt(ExrLineOrder.values(), repr);
    }
}

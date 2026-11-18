package com.vke.core.file.exr.head.attr;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import com.vke.core.file.exr.head.ExrLevelMode;
import com.vke.core.file.exr.head.ExrRoundingMode;
import com.vke.core.file.utils.BitPackerUtils;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class TileDescAttr implements ExrAttr {
    public final int xSize, ySize;
    public final ExrLevelMode levelMode;
    public final ExrRoundingMode roundingMode;

    public TileDescAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 4*2+1);
        this.xSize = DataUtils.readU32LittleEndian(stream);
        this.ySize = DataUtils.readU32LittleEndian(stream);
        int mode = DataUtils.readU8(stream);
        int[] unpacked = BitPackerUtils.unpackU4LE(mode, 2);
        int reprLevelMode = unpacked[0];
        int reprRoundingMode = unpacked[1];
        this.levelMode = IntEnum.fromInt(ExrLevelMode.values(), reprLevelMode);
        this.roundingMode = IntEnum.fromInt(ExrRoundingMode.values(), reprRoundingMode);
    }
}

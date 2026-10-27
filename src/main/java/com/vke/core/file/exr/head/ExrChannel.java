package com.vke.core.file.exr.head;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public record ExrChannel(String name, ExrPixelType pixelType, boolean pLinear, byte[] reserved, int xSampling, int ySampling) {
    public ExrChannel(InputStream stream) throws IOException {
        this(
            DataUtils.readNullStringUTF8(stream), //1+1+4+1+3+4+4
            IntEnum.fromInt(ExrPixelType.values(), DataUtils.readU32LittleEndian(stream)),
            DataUtils.readFlag(stream),
            stream.readNBytes(3),
            DataUtils.readU32LittleEndian(stream),
            DataUtils.readU32LittleEndian(stream)
        );
    }
}

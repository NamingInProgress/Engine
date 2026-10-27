package com.vke.core.file.exr.head.attr;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import com.vke.core.file.exr.head.ExrEnvMap;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public class EnvMapAttr implements ExrAttr {
    public final ExrEnvMap envMap;

    public EnvMapAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 1);
        int repr = DataUtils.readU8(stream);
        this.envMap = IntEnum.fromInt(ExrEnvMap.values(), repr);
    }
}

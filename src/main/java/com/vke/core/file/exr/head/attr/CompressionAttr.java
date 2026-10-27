package com.vke.core.file.exr.head.attr;

import com.vke.api.rendering.abstraction.renderer.IntEnum;
import com.vke.core.file.exr.head.ExrCompression;
import com.vke.core.file.utils.DataUtils;
import com.vke.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class CompressionAttr implements ExrAttr {
    public final ExrCompression compression;

    public CompressionAttr(InputStream stream, int size) throws IOException {
        assertSize(size, 1);
        int repr = DataUtils.readU8(stream);
        this.compression = IntEnum.fromInt(ExrCompression.values(), repr);
        if (Utils.TRUE) return;
        for (ExrCompression supported : ExrCompression.SUPPORTED) {
            if (supported == compression) {
                return;
            }
        }
        throw new IOException("Compression method (" + compression + ") is currently not in the list of currently support exr compression methods. the list in question:\n" + Arrays.toString(ExrCompression.SUPPORTED));
    }
}

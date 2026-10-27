package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StringAttr implements ExrAttr {
    public final String value;

    public StringAttr(InputStream stream, int ignoreSize) throws IOException {
        int length = DataUtils.readU32LittleEndian(stream);
        byte[] bytes = stream.readNBytes(length);
        this.value = new String(bytes, StandardCharsets.UTF_8);
    }
}

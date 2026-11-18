package com.vke.core.file.exr.head.attr;

import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StringAttr implements ExrAttr {
    public final String value;

    public StringAttr(InputStream stream, int size) throws IOException {
        byte[] bytes = stream.readNBytes(size);
        this.value = new String(bytes, StandardCharsets.UTF_8);
    }
}

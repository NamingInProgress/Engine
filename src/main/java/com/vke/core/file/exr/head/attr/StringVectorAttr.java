package com.vke.core.file.exr.head.attr;

import com.carrotsearch.hppc.ObjectArrayList;
import com.vke.core.file.utils.DataUtils;
import com.vke.utils.io.PositionedInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StringVectorAttr implements ExrAttr {
    public final String[] strings;

    public StringVectorAttr(InputStream stream, int size) throws IOException {
        PositionedInputStream wrapperStream = new PositionedInputStream(stream);
        ObjectArrayList<String> strings = new ObjectArrayList<>();
        while (wrapperStream.getPosition() <= size) {
            int length = DataUtils.readU32LittleEndian(wrapperStream);
            byte[] bytes = wrapperStream.readNBytes(length);
            strings.add(new String(bytes, StandardCharsets.UTF_8));
        }
        this.strings = strings.toArray(String.class);
    }
}

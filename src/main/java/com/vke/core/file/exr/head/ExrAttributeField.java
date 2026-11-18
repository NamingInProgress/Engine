package com.vke.core.file.exr.head;

import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.head.attr.ExrAttr;
import com.vke.core.file.utils.DataUtils;
import com.vke.utils.io.LimitedInputStream;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

public class ExrAttributeField {
    public final String name;
    public final String type;
    public final int size;
    public final @Nullable ExrAttr value;

    public ExrAttributeField(ExrDecoder dec, InputStream stream) throws IOException {
        this.name = dec.readExrStringNull(stream);
        System.out.print(name);
        this.type = dec.readExrStringNull(stream);
        System.out.println(" of " + type);
        this.size = DataUtils.readU32LittleEndian(stream);
        ExrAttr.Factory factory = dec.getAttrFactory(type);
        if (factory != null) {
            InputStream valueStream = new LimitedInputStream(stream, size);
            this.value = factory.read(valueStream, size);
        } else {
            this.value = null;
            stream.skipNBytes(size);
        }
    }
}

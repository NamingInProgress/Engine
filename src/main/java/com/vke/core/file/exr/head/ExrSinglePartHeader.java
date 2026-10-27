package com.vke.core.file.exr.head;

import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.utils.DataUtils;
import com.vke.utils.io.BytesInFrontOfInputStream;

import java.io.IOException;
import java.io.InputStream;

public class ExrSinglePartHeader {
    public final ExrAttrMap attrs;

    public ExrSinglePartHeader(ExrDecoder dec, InputStream stream) throws IOException {
        this.attrs = new ExrAttrMap();

        int next;
        while ((next = DataUtils.readU8(stream)) > 0) {
            InputStream attrStream = new BytesInFrontOfInputStream(stream, (byte) next);
            ExrAttributeField field = new ExrAttributeField(dec, attrStream);
            this.attrs.put(field.name, field.value);
        }
    }
}

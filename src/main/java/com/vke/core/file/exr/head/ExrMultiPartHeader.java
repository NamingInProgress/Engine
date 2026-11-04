package com.vke.core.file.exr.head;

import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.utils.DataUtils;
import com.vke.utils.Utils;
import com.vke.utils.io.BytesInFrontOfInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExrMultiPartHeader implements ExrHeaders {
    private final List<ExrHeader> headers;

    public ExrMultiPartHeader(ExrDecoder dec, InputStream stream) throws IOException {
        this.headers = new ArrayList<>();
        int next;
        while ((next = DataUtils.readU8(stream)) > 0) {
            InputStream headerStream = new BytesInFrontOfInputStream(stream, (byte) next);
            headers.add(new ExrHeader(dec, headerStream));
        }
    }

    @Override
    public ExrHeader getHeader(int index) throws IOException {
        if (!Utils.verifyArrayIndex(index, headers.size())) {
            throw new IOException("Illegal header index: " + index);
        }
        return headers.get(index);
    }
}

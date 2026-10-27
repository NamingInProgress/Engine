package com.vke.core.file.exr.singlepart;

import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.head.ExrSinglePartHeader;

import java.io.IOException;
import java.io.InputStream;

public class ExrSinglePartDecoder {
    private final ExrSinglePartHeader header;

    public ExrSinglePartDecoder(ExrDecoder dec, InputStream stream) throws IOException {
        this.header = new ExrSinglePartHeader(dec, stream);
    }
}

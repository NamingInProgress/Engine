package com.vke.core.file.exr.extract;

import com.vke.core.file.exr.ExrDecoder;

import java.io.InputStream;

public abstract class ExrAbstractExtractor implements ExrExtractor {
    protected final ExrDecoder decoder;
    protected final InputStream stream;

    protected ExrAbstractExtractor(ExrDecoder decoder, InputStream stream) {
        this.decoder = decoder;
        this.stream = stream;
    }
}

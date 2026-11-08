package com.vke.core.file.exr.extract;

import com.vke.core.file.exr.ExrDecoder;

import java.io.IOException;
import java.io.InputStream;

public class NoopDebugScanlineExtractor extends ExrScanlineExtractor {
    public NoopDebugScanlineExtractor(ExrDecoder decoder, InputStream stream) {
        super(decoder, stream);
    }

    public void skipAllScanlines() throws IOException {
        more = true;
        while (more) {
            readScanlines();
        }
    }
}

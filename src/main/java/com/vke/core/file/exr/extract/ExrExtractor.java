package com.vke.core.file.exr.extract;

import java.io.IOException;

public interface ExrExtractor {
    void readChunk(int part) throws IOException;

    boolean isSatisfied();
}

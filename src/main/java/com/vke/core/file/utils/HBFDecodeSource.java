package com.vke.core.file.utils;

import java.io.IOException;

public interface HBFDecodeSource<T> {
    void parseHeader() throws IOException;
    T nextByte() throws IOException;
    void parseFooter() throws IOException;
}

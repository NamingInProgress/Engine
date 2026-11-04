package com.vke.core.file.exr.head;

import java.io.IOException;

public interface ExrHeaders {
    ExrHeader getHeader(int index) throws IOException;
}

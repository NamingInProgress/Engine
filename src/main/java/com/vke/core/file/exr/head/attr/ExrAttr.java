package com.vke.core.file.exr.head.attr;

import java.io.IOException;
import java.io.InputStream;

public interface ExrAttr {
    default void assertSize(int size, int expected) throws IOException {
        if (size != expected) throw new IOException("Illegal size received for an attribute! Got: " + size + ", Want: " + expected);
    }

    @FunctionalInterface
    interface Factory {
        ExrAttr read(InputStream stream, int size) throws IOException;
    }
}

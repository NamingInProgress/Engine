package com.vke.api.file;

import org.jetbrains.annotations.Nullable;

public interface LazyArray<T> {
    @Nullable T next() throws DecodeException;
}

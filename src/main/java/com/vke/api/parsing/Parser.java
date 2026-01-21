package com.vke.api.parsing;

public interface Parser<T> {
    T parse(SourceCode source) throws Exception;
}

package com.vke.api.file;

import com.vke.utils.Identifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface LazyDecoder<T> extends AnyDecoder {
    LazyArray<T> decodeLazy(InputStream stream) throws DecodeException;

    default LazyArray<T> decodeLazy(String path) throws DecodeException {
        return decodeLazy(LazyDecoder.class.getResourceAsStream(path));
    }

    default LazyArray<T> decodeLazy(Identifier identifier) throws DecodeException {
        try {
            return decodeLazy(identifier.asInputStream());
        } catch (IOException e) {
            throw new DecodeException(e);
        }
    }

    default LazyArray<T> decodeLazy(byte[] data) throws DecodeException {
        return decodeLazy(new ByteArrayInputStream(data));
    }
}

package com.vke.api.file;

import com.vke.utils.Identifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface Decoder<F> extends AnyDecoder {
    F decode(InputStream input) throws DecodeException;

    default F decode(String path) throws DecodeException {
        return decode(Decoder.class.getResourceAsStream(path));
    }

    default F decode(Identifier identifier) throws DecodeException {
        try {
            return decode(identifier.asInputStream());
        } catch (IOException e) {
            throw new DecodeException(e);
        }
    }

    default F decode(byte[] data) throws DecodeException {
        return decode(new ByteArrayInputStream(data));
    }
}

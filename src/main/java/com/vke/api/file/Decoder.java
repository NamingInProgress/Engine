package com.vke.api.file;

import com.vke.core.Context;
import com.vke.core.FileIdentifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface Decoder<F> extends AnyDecoder {
    F decode(Context context, InputStream input) throws DecodeException;

    default F decode(Context context, String path) throws DecodeException {
        return decode(context, Decoder.class.getResourceAsStream(path));
    }

    default F decode(Context context, FileIdentifier identifier) throws DecodeException {
        try {
            return decode(context, identifier.openInputStream());
        } catch (IOException e) {
            throw new DecodeException(e);
        }
    }

    default F decode(Context context, byte[] data) throws DecodeException {
        return decode(context, new ByteArrayInputStream(data));
    }
}

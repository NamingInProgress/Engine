package com.vke.core.file.decoders;

import com.vke.api.file.DecodeException;
import com.vke.api.file.Decoder;
import com.vke.core.file.png.PngFile;

import java.io.IOException;
import java.io.InputStream;

public class PngDecoder implements Decoder<PngFile> {
    public static final String KEY = "png";

    @Override
    public PngFile decode(InputStream input) throws DecodeException {
        try {
            return new PngFile(input);
        } catch (IOException e) {
            throw new DecodeException(e);
        }
    }
}

package com.vke.core.file.exr.head;

import com.vke.core.file.exr.ExrDecoder;
import com.vke.utils.Utils;

import java.io.IOException;
import java.io.InputStream;

public class ExrSinglePartHeader extends ExrHeader implements ExrHeaders {
    public ExrSinglePartHeader(ExrDecoder dec, InputStream stream) throws IOException {
        super(dec, stream);
    }

    @Override
    @SuppressWarnings("all")
    public ExrHeader getHeader(int index) throws IOException {
        return index == 0 ? this : Utils.ARGHH("Header index out of bounds -> this is a singlepart!");
    }
}

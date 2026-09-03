package com.vke.core.file.decoders;

import com.vke.api.file.DecodeException;
import com.vke.api.file.Decoder;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.core.Context;
import com.vke.core.file.png.PngFile;
import com.vke.core.services2.Services;

import java.io.IOException;
import java.io.InputStream;

public class PngDecoder implements Decoder<PngFile> {
    public static final String KEY = "png";

    @Override
    public PngFile decode(Context context, InputStream input) throws DecodeException {
        Renderer renderer = context.service(Services.RENDERER);
        try {
            return new PngFile(input, renderer.renderSystem().flipImages());
        } catch (IOException e) {
            throw new DecodeException(e);
        }
    }
}

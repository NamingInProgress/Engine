package com.vke.core.file.decoders;

import com.vke.api.file.DecodeException;
import com.vke.api.file.Decoder;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.core.Context;
import com.vke.core.file.exr.ExrChunkReader;
import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.extract.ExrBitmapExtractor;
import com.vke.core.image.Bitmap;
import com.vke.core.services2.Services;
import com.vke.utils.Utils;

import java.io.InputStream;

public class ExrBmpDecoder implements Decoder<Bitmap> {
    public static final String KEY = "exr-bmp";

    @Override
    public Bitmap decode(Context context, InputStream input) throws DecodeException {
        return Utils.chainExceptions(() -> {
            ExrDecoder decoder = new ExrDecoder(input);
            ExrChunkReader chunkReader = decoder.createChunkReader();
            Renderer renderer = context.service(Services.RENDERER);
            ExrBitmapExtractor extractor = decoder.createBitmapExtractor(0, renderer.renderSystem().flipImages());
            chunkReader.assignExtractor(0, extractor);
            chunkReader.readAllChunks();
            return extractor.getBitmap();
        });
    }
}

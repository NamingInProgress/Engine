package com.vke.api.file;

import com.vke.core.file.decoders.*;
import com.vke.core.file.wav.WAVDecoder;

import java.util.HashMap;
import java.util.function.Supplier;

public class Decoders {
    private static final Decoders INSTANCE = new Decoders();

    private final HashMap<String, Supplier<AnyDecoder>> decoders;

    private Decoders() {
        decoders = new HashMap<>();

        decoders.put(GzipDecoder.KEY, GzipDecoder::new);
        decoders.put(ZlibDecoder.KEY, ZlibDecoder::new);
        decoders.put(WAVDecoder.KEY, WAVDecoder::new);
        decoders.put(PngDecoder.KEY, PngDecoder::new);
        decoders.put(ObjDecoder.KEY, ObjDecoder::new);
        decoders.put(ExrBmpDecoder.KEY, ExrBmpDecoder::new);
    }

    @SuppressWarnings("unchecked")
    public static <D extends AnyDecoder> D make(String type) {
        try {
            return (D) INSTANCE.decoders.get(type).get();
        } catch (ClassCastException ignore) {
            return null;
        }
    }
}


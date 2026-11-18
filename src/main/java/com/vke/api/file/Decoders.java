package com.vke.api.file;

import com.vke.core.file.decoders.GzipDecoder;
import com.vke.core.file.decoders.ObjDecoder;
import com.vke.core.file.decoders.PngDecoder;
import com.vke.core.file.decoders.ZlibDecoder;
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


package com.vke.api.file;

import com.vke.core.file.decoders.GzipDecoder;
import com.vke.core.file.decoders.ZlibDecoder;

import java.util.HashMap;

public class Decoders {
    private static final Decoders INSTANCE = new Decoders();

    private final HashMap<String, AnyDecoder> decoders;

    private Decoders() {
        decoders = new HashMap<>();

        decoders.put(GzipDecoder.KEY, new GzipDecoder());
        decoders.put(ZlibDecoder.KEY, new ZlibDecoder());
    }

    @SuppressWarnings("unchecked")
    public static <D extends AnyDecoder> D find(String type) {
        try {
            return (D) INSTANCE.decoders.get(type);
        } catch (ClassCastException ignore) {
            return null;
        }
    }
}


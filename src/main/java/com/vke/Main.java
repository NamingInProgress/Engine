package com.vke;

import com.vke.api.file.Decoder;
import com.vke.api.file.Decoders;
import com.vke.api.file.LazyDecoder;
import com.vke.api.file.LazyArray;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        try {
            VKEngine engine = new VKEngine(new EngineCreateInfo());
            Decoder<byte[]> decoder = Decoders.find("gzip");
            LazyDecoder<Byte> decoder2 = Decoders.find("gzip");
            LazyArray<Byte> data = decoder2.decodeLazy("test.gz");


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
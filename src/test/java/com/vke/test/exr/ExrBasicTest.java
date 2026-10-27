package com.vke.test.exr;

import com.vke.core.FileIdentifier;
import com.vke.core.Identifier;
import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.singlepart.ExrSinglePartDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class ExrBasicTest {
    public static void main(String[] args) throws IOException, URISyntaxException {
        InputStream testImageStream = new FileIdentifier(true, Identifier.of("lol:BrightRings.exr"), "").openInputStream();
        ExrDecoder decoder = new ExrDecoder(testImageStream);
        if (decoder.head.isSingleTile) {
            System.out.println("single tile");
        } else if (decoder.head.isMultipart) {
            System.out.println("multipart");
        } else {
            System.out.println("single scanline");
            ExrSinglePartDecoder spDecoder = new ExrSinglePartDecoder(decoder, testImageStream);
        }
    }
}

package com.vke.test.exr;

import com.vke.core.FileIdentifier;
import com.vke.core.Identifier;
import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.ExrPart;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;

public class ExrBasicTest {
    public static void main(String[] args) throws IOException, URISyntaxException {
        InputStream testImageStream = new FileIdentifier(true, Identifier.of("lol:BrightRings.exr"), "").openInputStream();
        ExrDecoder decoder = new ExrDecoder(testImageStream);
        ExrPart part = decoder.parts[0];
        System.out.println(Arrays.toString(part.offsetTable()));
    }
}

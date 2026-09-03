package com.vke.test.jpeg;

import com.vke.core.FileIdentifier;
import com.vke.core.file.jpeg.JpegFile;
import com.vke.core.file.jpeg.jfif.JfifDecoder;

import java.io.IOException;
import java.io.InputStream;

public class JpegMarkerTest {
    public static void main(String[] args) throws IOException {
        FileIdentifier ident = FileIdentifier.of("./assets:vke:/assets/jpeg/scaryvulkan.jpeg");
        InputStream stream = ident.openInputStream();
        JpegFile file = new JpegFile(stream);
    }
}

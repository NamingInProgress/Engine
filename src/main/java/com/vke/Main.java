package com.vke;

import com.vke.api.file.DecodeException;
import com.vke.api.file.Decoder;
import com.vke.api.file.Decoders;
import com.vke.core.file.png.PixelOutput;
import com.vke.core.file.png.PngFile;
import com.vke.core.file.wav.WAVFile;
import com.vke.utils.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) throws InterruptedException, DecodeException, IOException {
        Decoder<WAVFile> decoder = Decoders.find("wav");
        WAVFile file = decoder.decode(new Identifier("test.wav").asInputStream());
        System.out.println(file.getFmtPayload());

        System.exit(0);

        try {
            InputStream stream = new Identifier("adam7.png").asInputStream();
            Decoder<PngFile> pngDecoder = Decoders.find("png");
            PngFile pngFile = pngDecoder.decode(stream);
            PixelOutput output = pngFile.getOutput();
            BufferedImage image = output.toJavaImage();
            ImageIO.write(image, "PNG", new File("test.png"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
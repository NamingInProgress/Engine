package com.vke;

import com.vke.core.file.png.PixelOutput;
import com.vke.core.file.png.PngFile;
import com.vke.utils.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        try {
            InputStream stream = new Identifier("transparency.png").asInputStream();
            PngFile pngFile = new PngFile(stream);
            PixelOutput output = pngFile.getOutput();
            BufferedImage image = output.toJavaImage();
            ImageIO.write(image, "PNG", new File("test.png"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
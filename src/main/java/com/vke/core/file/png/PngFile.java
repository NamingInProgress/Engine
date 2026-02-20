package com.vke.core.file.png;

import com.vke.core.file.io.bit.ShittyBitInputStream;
import com.vke.core.file.png.chunks.*;
import com.vke.core.file.zlib.ZlibDecompressor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PngFile {
    private static final byte[] SIGNATURE = {
            (byte) 137,
            (byte) 80,
            (byte) 78,
            (byte) 71,
            (byte) 13,
            (byte) 10,
            (byte) 26,
            (byte) 10
    };

    private final PngInfo pngInfo;
    private final Pixels output;

    public PngFile(InputStream inputStream) throws IOException {
        if (!verifySignature(inputStream)) {
            throw new IOException("Illegal PNG file received!");
        }

        PngChunk first = PngChunk.readNextChunk(inputStream);
        if (!(first instanceof IHDR ihdr)) {
            throw new IOException("Illegal chunk found! Expected IHDR!");
        }

        pngInfo = ihdr.getInfo();

        int[][] palette = null;
        ZlibDecompressor idatDecompressor = new ZlibDecompressor(new ShittyBitInputStream(null));

        loop:
        while (true) {
            PngChunk chunk = PngChunk.readNextChunk(inputStream);
            //System.out.println(chunk.getChunkTypeString());
            switch (chunk) {
                case IEND _:
                    break loop;
                case IDAT idat:
                    idatDecompressor.appendData(new ByteArrayInputStream(idat.getZlibData()));
                    continue;
                case PLTE plte:
                    palette = plte.getPalette();
                    continue;
                default:
                    break;
            }
        }


        if (pngInfo.interlacingMethod == PngInfo.InterlacingMethod.Adam7) {
            //then i hate my life
            output = Adam7ImageSampler.sample(pngInfo, idatDecompressor, palette);
        } else {
            output = SequentialImageSampler.sample(pngInfo, idatDecompressor, palette);
        }
    }

    public Pixels getOutput() {
        return output;
    }

    public PngInfo getPngInfo() {
        return pngInfo;
    }

    private boolean verifySignature(InputStream stream) throws IOException {
        for (byte value : SIGNATURE) {
            int b = stream.read();
            if (b < 0) return false;
            if (((byte) b) != value) return false;
        }
        return true;
    }
}

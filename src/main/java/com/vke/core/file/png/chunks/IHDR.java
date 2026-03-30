package com.vke.core.file.png.chunks;

import com.vke.core.file.png.PngInfo;

import java.io.IOException;
import java.io.InputStream;

public class IHDR extends PngChunk {
    private final PngInfo info;

    public IHDR() {
        info = new PngInfo();
    }

    @Override
    protected void readContents(InputStream stream) throws IOException {
        if (dataLength != 13) {
            throw new IOException("IHDR chunk must be 13 bytes");
        }

        info.width = readInt32(stream);
        info.height = readInt32(stream);
        info.sampleBitDepth = readInt8(stream);

        int colorType = readInt8(stream);
        int compressionMethod = readInt8(stream);
        int filterMethod = readInt8(stream);
        int interlaceMethod = readInt8(stream);

        // Validate compression
        if (compressionMethod != 0) {
            throw new IOException("Unsupported PNG compression method: " + compressionMethod);
        }

        // Validate filter
        if (filterMethod != 0) {
            throw new IOException("Unsupported PNG filter method: " + filterMethod);
        }

        // Interlace
        switch (interlaceMethod) {
            case 0:
                info.interlacingMethod = PngInfo.InterlacingMethod.Sequential;
                break;
            case 1:
                info.interlacingMethod = PngInfo.InterlacingMethod.Adam7;
                break;
            default:
                throw new IOException("Invalid interlace method: " + interlaceMethod);
        }

        // Color type mapping
        switch (colorType) {
            case 0: // Grayscale
                info.pixelType = PngInfo.PixelType.GrayScale;
                info.hasColorPalette = false;
                info.hasAlphaChannel = false;
                break;

            case 2: // Truecolor
                info.pixelType = PngInfo.PixelType.TrueColor;
                info.hasColorPalette = false;
                info.hasAlphaChannel = false;
                break;

            case 3: // Indexed-color
                info.pixelType = PngInfo.PixelType.PaletteIndex;
                info.hasColorPalette = true;
                info.hasAlphaChannel = false;
                break;

            case 4: // Grayscale + Alpha
                info.pixelType = PngInfo.PixelType.GrayScale;
                info.hasColorPalette = false;
                info.hasAlphaChannel = true;
                break;

            case 6: // Truecolor + Alpha
                info.pixelType = PngInfo.PixelType.TrueColor;
                info.hasColorPalette = false;
                info.hasAlphaChannel = true;
                break;

            default:
                throw new IOException("Unsupported PNG color type: " + colorType);
        }

        validateBitDepth(colorType, info.sampleBitDepth);
    }

    private void validateBitDepth(int colorType, int bitDepth) throws IOException {
        switch (colorType) {
            case 0: // grayscale
                if (!(bitDepth == 1 || bitDepth == 2 || bitDepth == 4 ||
                        bitDepth == 8 || bitDepth == 16))
                    throw new IOException("Invalid bit depth for grayscale");
                break;

            case 2: // truecolor
            case 4: // grayscale+alpha
            case 6: // truecolor+alpha
                if (!(bitDepth == 8 || bitDepth == 16))
                    throw new IOException("Invalid bit depth for truecolor/alpha");
                break;

            case 3: // indexed
                if (!(bitDepth == 1 || bitDepth == 2 ||
                        bitDepth == 4 || bitDepth == 8))
                    throw new IOException("Invalid bit depth for indexed color");
                break;

            default:
                throw new IOException("Unknown color type");
        }
    }

    public PngInfo getInfo() {
        return info;
    }
}

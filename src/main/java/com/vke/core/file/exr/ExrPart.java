package com.vke.core.file.exr;

import com.vke.core.file.exr.head.ExrHeader;
import com.vke.core.file.exr.head.ExrHeaderHead;
import com.vke.core.file.exr.head.attr.StringAttr;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public record ExrPart(ExrHeader header, long[] offsetTable) {
    public ExrPart(ExrHeader header, InputStream stream) throws IOException {
        int offsetCount = header.computeOffsetCount();
        long[] offsetTable = new long[offsetCount];
        for (int i = 0; i < offsetCount; i++) {
            offsetTable[i] = DataUtils.readU64LittleEndian(stream);
        }
        //set your java to 25 bear
        this(header, offsetTable);
    }

    public ImageType getImageType(ExrHeaderHead head) {
        if (head.isMultipart) {
            StringAttr typeAttr = (StringAttr) header.attrs.get("type");
            String value = typeAttr.value;
            return switch (value) {
                case "scanlineimage" -> ImageType.Scanline;
                case "tiledimage" -> ImageType.Tile;
                case "deepscanline" -> ImageType.DeepScan;
                case "deeptile" -> ImageType.DeepTile;
                default -> throw new IllegalArgumentException(value);
            };
        } else {
            if (head.isSingleTile) {
                if (head.hasDeepData) {
                    return ImageType.DeepTile;
                } else {
                    return ImageType.Tile;
                }
            } else {
                if (head.hasDeepData) {
                    return ImageType.DeepScan;
                } else {
                    return ImageType.Scanline;
                }
            }
        }
    }

    public enum ImageType {
        Scanline,
        Tile,
        DeepScan,
        DeepTile
    }
}

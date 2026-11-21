package com.vke.core.file.exr.extract;

import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.head.ExrCompression;
import com.vke.core.file.exr.head.ExrHeader;
import com.vke.core.file.exr.head.attr.Box2iAttr;
import com.vke.core.file.utils.DataUtils;
import com.vke.utils.IntUtils;

import java.io.IOException;
import java.io.InputStream;

public abstract class ExrTileExtractor extends ExrAbstractExtractor{
    private boolean satisfied;

    protected int xPos, yPos;
    protected int tileScans;
    protected int scanPixels;
    private int totalTiles;

    protected byte[] samples;

    protected ExrTileExtractor(ExrDecoder decoder, InputStream stream) {
        super(decoder, stream);
    }

    @Override
    public void readChunk(int part) throws IOException {
        ExrHeader header = decoder.parts[part].header();
        Box2iAttr dw = header.dataWindow;
        assert header.tiles != null;
        int tileWidth = header.tiles.xSize;
        int tileHeight = header.tiles.ySize;

        int tileX = DataUtils.readU32LittleEndian(stream);
        int tileY = DataUtils.readU32LittleEndian(stream);
        int levelX = DataUtils.readU32LittleEndian(stream);
        int levelY = DataUtils.readU32LittleEndian(stream);
        int dataSize = DataUtils.readU32LittleEndian(stream);

        xPos = tileX * tileWidth;
        yPos = tileY * tileHeight;

        int tilesAcross = IntUtils.fastCeilDivPositive(dw.width(), tileWidth);
        int tilesDown = IntUtils.fastCeilDivPositive(dw.height(), tileHeight);

        int xMax = dw.xMax;
        int yMax = dw.yMax;
        tileScans = Math.min(tileHeight, yMax - tileY + 1);
        scanPixels = Math.min(tileWidth, xMax - tileX + 1);
        totalTiles++;

        if (totalTiles >= tilesAcross * tilesDown) {
            satisfied = true;
        }

        int uncompressedSize = header.computeScanBlockByteSize(tileX, tileY, scanPixels, tileScans);
        if (samples == null || samples.length < uncompressedSize) {
            samples = new byte[uncompressedSize];
        }

        ExrCompression cmpr = header.compression.compression;
        long bytesRead = decoder.decompress(samples, 0, uncompressedSize, cmpr, dataSize);
        if (bytesRead < dataSize) {
            int remaining = (int) (dataSize - bytesRead);
            stream.skipNBytes(remaining);
        }

        onTileRead(part);
    }

    protected abstract void onTileRead(int part) throws IOException;

    @Override
    public boolean isSatisfied() {
        return satisfied;
    }
}

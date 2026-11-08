package com.vke.core.file.exr.extract;

import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.head.ExrCompression;
import com.vke.core.file.exr.head.ExrHeader;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public abstract class ExrScanlineExtractor extends ExrExtractor {
    protected boolean more;
    protected int part;
    protected int yPos;

    protected byte[] samples;
    protected int scani;

    protected int blockScans;

    private int totalScanlines;

    protected ExrScanlineExtractor(ExrDecoder decoder, InputStream stream) {
        super(decoder, stream);
    }

    public void readScanlines() throws IOException {
        scani = 0;
        more = true;
        part = (int) nextChunk();
        ExrHeader header = decoder.parts[part].header();
        ExrCompression cmpr = header.compression.compression;
        yPos = DataUtils.readU32LittleEndian(stream);
        int yMax = header.dataWindow.yMax;
        blockScans = Math.min(cmpr.numberOfScanlinesPerBlock(), yMax - yPos + 1);
        totalScanlines += blockScans;
        if (totalScanlines >= header.dataWindow.height()) {
            more = false;
            return;
        }
        int dataSize = DataUtils.readU32LittleEndian(stream);
        int uncompressedSize = header.computeScanBlockByteSize(yPos, blockScans);
        if (samples == null || samples.length < uncompressedSize) {
            samples = new byte[uncompressedSize];
        }
        long bytesRead = decompress(samples, 0, uncompressedSize, cmpr);
        if (bytesRead < dataSize) {
            int remaining = (int) (dataSize - bytesRead);
            stream.skipNBytes(remaining);
        }
    }
}

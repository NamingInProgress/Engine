package com.vke.core.file.exr.extract;

import com.vke.core.file.exr.ExrDecoder;
import com.vke.core.file.exr.head.ExrCompression;
import com.vke.core.file.exr.head.ExrHeader;
import com.vke.core.file.utils.DataUtils;

import java.io.IOException;
import java.io.InputStream;

public abstract class ExrScanlineExtractor extends ExrAbstractExtractor implements ExrExtractor {
    protected boolean more;
    protected int yPos;

    protected byte[] samples;
    protected int scani;

    protected int blockScans;

    private int totalScanlines;

    protected ExrScanlineExtractor(ExrDecoder decoder, InputStream stream) {
        super(decoder, stream);
    }

    @Override
    public void readChunk(int part) throws IOException {
        scani = 0;
        more = true;
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
        long bytesRead = decoder.decompress(samples, 0, uncompressedSize, cmpr, dataSize);
        if (bytesRead < dataSize) {
            int remaining = (int) (dataSize - bytesRead);
            stream.skipNBytes(remaining);
        }

        onScanlinesRead(part);
    }

    protected abstract void onScanlinesRead(int part) throws IOException;
}

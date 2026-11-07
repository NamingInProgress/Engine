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

    private int block_scans;

    protected ExrScanlineExtractor(ExrDecoder decoder, InputStream stream) {
        super(decoder, stream);
    }

    public void readScanlines() throws IOException {
        scani = 0;
        part = (int) nextChunk();
        ExrHeader header = decoder.parts[part].header();
        ExrCompression cmpr = header.compression.compression;
        block_scans = cmpr.numberOfScanlinesPerBlock();
        yPos = DataUtils.readU32LittleEndian(stream);
        int dataSize = DataUtils.readU32LittleEndian(stream);
        if (samples.length < dataSize) {
            samples = new byte[dataSize];
        }
        decompress(samples, 0, dataSize, cmpr);
    }

    protected void consume() {
        scani++;
        if (scani >= block_scans) {
            more = false;
        }
    }
}

package com.vke.core.file.exr;

import com.vke.api.logger.Logger;
import com.vke.core.file.exr.extract.ExrExtractor;
import com.vke.core.file.exr.head.ExrHeader;
import com.vke.core.file.exr.head.ExrHeaderHead;
import com.vke.core.file.exr.head.attr.StringAttr;
import com.vke.core.file.utils.DataUtils;
import com.vke.core.logger.LoggerFactory;
import com.vke.utils.Utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class ExrChunkReader {
    private final Logger logger;

    private final int nParts;
    private final ExrPart[] parts;
    private final ExrExtractor[] extractors;
    private int extractorCount;
    private final ExrHeaderHead head;
    private final InputStream stream;

    ExrChunkReader(ExrPart[] parts, ExrHeaderHead head, InputStream stream) {
        this.head = head;
        this.stream = stream;
        this.logger = LoggerFactory.get("Exr");

        this.nParts = parts.length;
        this.extractors = new ExrExtractor[nParts];
        this.parts = parts;
    }

    public void assignExtractor(int part, ExrExtractor extractor) {
        if (!Utils.verifyArrayIndex(part, extractors)) {
            logger.error("Could not assign extractor, part %d is not valid! Available parts are: 0 to %d", part, nParts);
        } else {
            this.extractors[part] = extractor;
            extractorCount++;
        }
    }

    private int nextChunkPart() throws IOException {
        if (head.isMultipart) {
            return (int) DataUtils.readU64LittleEndian(stream);
        } else {
            return 0;
        }
    }

    private void skipChunk(int part) throws IOException {
        ExrHeader header = parts[part].header();
        if (head.isMultipart) {
            StringAttr typeAttr = (StringAttr) header.attrs.get("type");
            String value = typeAttr.value;
            switch (value) {
                case "scanlineimage" -> skipScanline();
                case "tiledimage" -> skipTile();
                case "deepscanline" -> skipDeepScan();
                case "deeptile" -> skipDeepTile();
                default -> logger.fatal("Unrecognized type!!! This is faulty -> %s", value);
            }
        } else {
            if (head.isSingleTile) {
                if (head.hasDeepData) {
                    skipDeepTile();
                } else {
                    skipTile();
                }
            } else {
                if (head.hasDeepData) {
                    skipDeepScan();
                } else {
                    skipScanline();
                }
            }
        }
    }

    private void skipScanline() throws IOException {
        //ypos
        stream.skipNBytes(4);
        int dataSize = DataUtils.readU32LittleEndian(stream);
        stream.skipNBytes(dataSize);
    }

    private void skipTile() throws IOException {
        //tile pos
        stream.skipNBytes(16);
        int dataSize = DataUtils.readU32LittleEndian(stream);
        stream.skipNBytes(dataSize);
    }

    private void skipDeepScan() throws IOException {
        stream.skipNBytes(4);

        int packedPixelOffsetTableSize = DataUtils.readU32LittleEndian(stream);
        int packedSampleDataSize = DataUtils.readU32LittleEndian(stream);

        long toSkip = 8 + packedPixelOffsetTableSize + packedSampleDataSize;
        stream.skipNBytes(toSkip);
    }

    private void skipDeepTile() throws IOException {
        stream.skipNBytes(16);

        int packedPixelOffsetTableSize = DataUtils.readU32LittleEndian(stream);
        int packedSampleDataSize = DataUtils.readU32LittleEndian(stream);

        long toSkip = 8 + packedPixelOffsetTableSize + packedSampleDataSize;
        stream.skipNBytes(toSkip);
    }

    public void readAllChunks() throws IOException {
        int finishedExtractors = 0;
        while (finishedExtractors < extractorCount) {
            try {
                int part = nextChunkPart();
                ExrExtractor extractor = extractors[part];
                if (extractor != null) {
                    extractor.readChunk(part);
                    if (extractor.isSatisfied()) {
                        finishedExtractors++;
                    }
                } else {
                    skipChunk(part);
                }
            } catch (EOFException eof) {
                break;
            }
        }
        if (finishedExtractors < extractorCount) {
            for (int part = 0; part < extractors.length; part++) {
                ExrExtractor extractor = extractors[part];
                if (!extractor.isSatisfied()) {
                    logger.error("The input is exhausted, but the extractor for part %d is not yet satisfied!", part);
                }
            }
        }
    }
}

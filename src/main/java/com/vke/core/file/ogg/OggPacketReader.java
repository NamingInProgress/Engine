package com.vke.core.file.ogg;

import com.vke.core.file.utils.DataUtils;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class OggPacketReader {
    private final InputStream stream;
    private final OggPageReader pageReader;

    private OggPageReader.PageHeader currentPage;
    private ByteArrayOutputStream currentPacket;
    private int segmentIndex;

    public OggPacketReader(InputStream stream) {
        this.stream = stream;
        this.pageReader = new OggPageReader(stream);
        this.currentPacket = new ByteArrayOutputStream();
    }

    public byte[] readNextPacket() throws IOException {
        if (currentPage == null) {
            currentPage = pageReader.parsePageHeader();
            if (currentPage == null) return null;
            segmentIndex = 0;
            if (!currentPage.packetContinuation()) {
                currentPacket = new ByteArrayOutputStream();
            }
        }
        int lacing = currentPage.segmentTable()[segmentIndex++];
        if (segmentIndex >= currentPage.segmentTable().length) {
            currentPage = null;
        }

        DataUtils.transferBytes(stream, currentPacket, lacing);
        if (lacing < 255) {
            byte[] bytes = currentPacket.toByteArray();
            currentPacket = new ByteArrayOutputStream();
            return bytes;
        } else {
            return readNextPacket();
        }
    }
}

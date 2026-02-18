package com.vke.core.file.wav;

import com.vke.core.file.wav.chunks.WAVfmtChunk;

public class WAVFile {
    private final WAVfmtChunk.Payload fmtPayload;
    private final byte[] rawSampleData;

    public WAVFile(WAVfmtChunk.Payload fmtPayload, byte[] rawSampleData) {
        this.fmtPayload = fmtPayload;
        this.rawSampleData = rawSampleData;
    }

    public WAVfmtChunk.Payload getFmtPayload() {
        return fmtPayload;
    }

    public byte[] getRawSampleData() {
        return rawSampleData;
    }
}

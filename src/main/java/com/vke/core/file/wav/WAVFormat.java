package com.vke.core.file.wav;

import com.vke.core.file.riff.RIFFFormat;
import com.vke.core.file.wav.chunks.WAVdataChunk;
import com.vke.core.file.wav.chunks.WAVfmtChunk;

public class WAVFormat extends RIFFFormat {
    @Override
    protected void registerChunks() {
        registerChunk(WAVdataChunk.NAME, new WAVdataChunk.Factory());
        registerChunk(WAVfmtChunk.NAME, new WAVfmtChunk.Factory());
    }
}

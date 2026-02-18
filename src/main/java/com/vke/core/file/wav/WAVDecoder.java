package com.vke.core.file.wav;

import com.vke.api.file.DecodeException;
import com.vke.core.file.riff.RIFFBinaryPayload;
import com.vke.core.file.riff.RIFFDecoder;
import com.vke.core.file.riff.chunks.RIFFRootChunk;
import com.vke.core.file.wav.chunks.WAVdataChunk;
import com.vke.core.file.wav.chunks.WAVfmtChunk;

import java.io.IOException;
import java.io.InputStream;

public class WAVDecoder extends RIFFDecoder<WAVFile> {
    public static final String KEY = "wav";

    public WAVDecoder() {
        super(new WAVFormat());
    }

    @Override
    public WAVFile decode(InputStream stream) throws DecodeException {
        RIFFRootChunk root = null;
        try {
            root = (RIFFRootChunk) format.readNextChunk(stream);
        } catch (IOException e) {
            throw new DecodeException(e);
        }
        //noinspection EqualsBetweenInconvertibleTypes
        if (!root.getFormType().equals("WAVE")) {
            throw new DecodeException("Illegal WAV file found! Expected formType 'WAVE'!");
        }

        WAVfmtChunk fmtChunk = (WAVfmtChunk) root.findChunksByName(WAVfmtChunk.NAME).next();
        WAVdataChunk dataChunk = (WAVdataChunk) root.findChunksByName(WAVdataChunk.NAME).next();

        WAVfmtChunk.Payload fmtPayload = (WAVfmtChunk.Payload) fmtChunk.payload();
        RIFFBinaryPayload rawSampleBytes = (RIFFBinaryPayload) dataChunk.payload();

        return new WAVFile(fmtPayload, rawSampleBytes.getData());
    }
}

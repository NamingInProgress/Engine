package com.vke.core.audio.decoder;

import com.vke.core.audio.pcm.PCMInfo;
import com.vke.utils.types.Container;

import java.io.IOException;

public interface AudioDecoder {
    PCMInfo decodeMeta() throws IOException;

    int decodeNextFrames(Container<float[][]> out, int offset) throws IOException;

    void createState(long framePos) throws IOException;
}

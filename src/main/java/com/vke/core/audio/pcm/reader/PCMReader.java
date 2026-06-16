package com.vke.core.audio.pcm.reader;

import com.vke.core.audio.pcm.PCMInfo;

public interface PCMReader {
    int fetchFrames(float[][] dst, int dstPos, int frames);
    void seek(long frame);
    long position();
    PCMInfo getInfo();
}

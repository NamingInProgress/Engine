package com.vke.core.audio.pcm;

public interface PCMReader {
    void fetchFrames(float[][] dst, int dstPos, int frames);
    void seek(long frame);
    PCMInfo getInfo();
}

package com.vke.core.audio.source;

import com.vke.core.audio.pcm.PCMInfo;
import com.vke.core.audio.pcm.reader.PCMReader;

public class VorbisPCMReader implements PCMReader {
    private long position;

    @Override
    public int fetchFrames(float[][] dst, int dstPos, int frames) {
        return 0;
    }

    @Override
    public void seek(long frame) {
        this.position = frame;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public PCMInfo getInfo() {
        return null;
    }
}

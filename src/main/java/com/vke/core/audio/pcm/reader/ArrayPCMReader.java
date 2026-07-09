package com.vke.core.audio.pcm.reader;

import com.vke.core.audio.pcm.PCMInfo;

import java.util.Arrays;

public class ArrayPCMReader implements PCMReader{
    private final float[][] pcm;
    private final PCMInfo info;
    private long position;

    public ArrayPCMReader(float[][] pcm, PCMInfo info) {
        this.pcm = pcm;
        this.info = info;
    }

    @Override
    public int fetchFrames(float[][] dst, int dstPos, int frames) {
        int toCopy = (int) Math.min(frames, pcm.length - position);
        System.arraycopy(pcm, (int) position, dst, dstPos, toCopy);
        position += frames;
        return toCopy;
    }

    @Override
    public void seek(long frame) {
        position = frame;
    }

    @Override
    public long position() {
        return position;
    }

    @Override
    public PCMInfo getInfo() {
        return info;
    }
}

package com.vke.core.audio.pcm;

public class PCMInfo {
    public final int sampleRate;
    public final int channels;
    public final int bitsPerSample;

    public PCMInfo(int sampleRate, int channels, int bitsPerSample) {
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.bitsPerSample = bitsPerSample;
    }

    public int bytesPerFrame() {
        return channels * (bitsPerSample / 8);
    }
}

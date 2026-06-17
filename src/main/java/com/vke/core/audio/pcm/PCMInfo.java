package com.vke.core.audio.pcm;

public class PCMInfo {
    public final int sampleRate;
    public final int channels;
    public final int bitsPerSample;
    public final long totalFrames;

    public final Object extension;

    public PCMInfo(int sampleRate, int channels, int bitsPerSample, long totalFrames) {
        this(sampleRate, channels, bitsPerSample, totalFrames, null);
    }

    public PCMInfo(int sampleRate, int channels, int bitsPerSample, long totalFrames, Object extension) {
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.bitsPerSample = bitsPerSample;
        this.totalFrames = totalFrames;
        this.extension = extension;
    }

    public int bytesPerFrame() {
        return channels * (bitsPerSample / 8);
    }

    @Override
    public String toString() {
        return "PCMInfo{" +
                "sampleRate=" + sampleRate +
                ", channels=" + channels +
                ", bitsPerSample=" + bitsPerSample +
                ", totalFrames=" + totalFrames +
                ", extension=" + extension +
                '}';
    }
}

package com.vke.core.audio.pcm;

public class EnginePCM {
    public static final int SAMPLE_RATE = 48000;
    public static final int BLOCK_SIZE = 1024;

    private float[][] frames;
    private int blockPos;
    private PCMReader reader;
    private PCMInfo info;

    private EnginePCM() {}

    public static EnginePCM fromReader(PCMReader reader) {
        PCMInfo info = reader.getInfo();
        EnginePCM pcm = new EnginePCM();
        pcm.frames = new float[BLOCK_SIZE][info.channels];
        pcm.blockPos = 0;
        pcm.reader = reader;
        pcm.info = info;
        pcm.fillBlock();
        return pcm;
    }

    private void fillBlock() {
        reader.fetchFrames(frames, 0, BLOCK_SIZE);
    }

    private void fillBlockRemaining() {
        reader.fetchFrames(frames, blockPos, BLOCK_SIZE - blockPos);
    }
}

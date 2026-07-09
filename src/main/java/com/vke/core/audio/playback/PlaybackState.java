package com.vke.core.audio.playback;

import com.vke.api.audio.playback.PlayingAudio;
import com.vke.core.audio.pcm.PCMInfo;
import com.vke.core.audio.pcm.reader.PCMReader;

public class PlaybackState implements PlayingAudio {
    public static final int SAMPLE_RATE = 48000;
    public static final int BLOCK_SIZE = 1024;

    private float[][] block;
    private int blockPos;
    private PCMReader reader;
    private PCMInfo info;

    private long blockSize;
    private boolean stopped;
    private boolean eof;

    private volatile float volume, pan;
    private volatile boolean looping;

    private PlaybackState() {}

    public static PlaybackState fromReader(PCMReader reader) {
        PCMInfo info = reader.getInfo();
        PlaybackState pcm = new PlaybackState();
        pcm.block = new float[BLOCK_SIZE][info.channels];
        pcm.blockPos = 0;
        pcm.reader = reader;
        pcm.info = info;
        int read = pcm.fillBlock();
        pcm.blockSize = read;
        pcm.eof = (read < BLOCK_SIZE);

        pcm.volume = 1f;
        pcm.pan = 0f;
        pcm.looping = false;
        return pcm;
    }

    private int fillBlock() {
        return reader.fetchFrames(block, 0, BLOCK_SIZE);
    }

    public float[] nextFrame() {
        if (blockPos >= blockSize && !eof) {
            int read = fillBlock();
            blockSize = read;
            blockPos = 0;
            if (read < BLOCK_SIZE) {
                eof = true;
            }
        }

        if (blockPos >= blockSize) {
            return new float[info.channels];
        }

        float[] frame = block[blockPos++];
        if (looping && !hasMoreFrames()) {
            eof = false;
            seekFrame(0);
        }
        return frame;
    }

    public boolean hasMoreFrames() {
        if (stopped) return false;

        return !(eof && blockPos >= blockSize);
    }

    public PCMInfo getOriginalInfo() {
        return info;
    }

    @Override
    public void setLooping(boolean looping) {
        this.looping = true;
    }

    @Override
    public void stop() {
        stopped = true;
    }

    public float getVolume() { return volume; }
    public float getPan() { return pan; }

    @Override
    public void setVolume(float volume) {
        this.volume = volume;
    }

    @Override
    public void setPan(float pan) {
        this.pan = pan;
    }

    @Override
    public void seek(long milliseconds) {
        long frame = (milliseconds / 1000) * SAMPLE_RATE;
        seekFrame(frame);
    }

    @Override
    public void seekFrame(long frame) {
        reader.seek(frame);
        blockPos = 0;
        int read = fillBlock();
        blockSize = read;
        eof = (read < BLOCK_SIZE);
    }
}

package com.vke.core.audio.pcm.reader;

import com.vke.core.audio.pcm.PCMInfo;

//ONLY USE FOR PRELOADED AUDIO SOURCES OR IT WILL DRAIN PERFORMANCE
public class SharedPCMReader implements PCMReader {
    private final PCMReader inner;
    private final Object lock;
    private long position;

    public SharedPCMReader(PCMReader inner) {
        this.inner = inner;
        this.lock = new Object();
    }

    @Override
    public int fetchFrames(float[][] dst, int dstPos, int frames) {
        synchronized (lock) {
            long p = inner.position();
            inner.seek(position);
            int read = inner.fetchFrames(dst, dstPos, frames);
            inner.seek(p + frames);
            position += frames;
            return read;
        }
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
        return inner.getInfo();
    }
}

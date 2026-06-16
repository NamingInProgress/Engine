package com.vke.core.audio.playback;

public interface Mixer {
    void newBlock();
    void mixNextFrame(float[] out);
}

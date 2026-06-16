package com.vke.api.audio.playback;

public interface PlayingAudio {
    void setLooping(boolean looping);
    void stop();
    void setVolume(float volume);
    void setPan(float pan);
    void seek(long milliseconds);
    void seekFrame(long frame);
}

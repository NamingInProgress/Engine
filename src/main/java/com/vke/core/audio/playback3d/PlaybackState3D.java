package com.vke.core.audio.playback3d;

import com.vke.api.audio.playback.PlayingAudio;
import com.vke.core.audio.playback.PlaybackState;

public class PlaybackState3D implements PlayingAudio {
    private final PlaybackState state;
    private final VkeSpeaker speaker;

    private volatile float targetLeftGain = 1.0f;
    private volatile float targetRightGain = 1.0f;

    private float currentLeftGain = 1.0f;
    private float currentRightGain = 1.0f;

    public PlaybackState3D(PlaybackState state, VkeSpeaker speaker) {
        this.state = state;
        this.speaker = speaker;
    }

    public void setTargetGains(float left, float right) {
        this.targetLeftGain = left;
        this.targetRightGain = right;
    }

    public float getTargetLeftGain() { return targetLeftGain; }
    public float getTargetRightGain() { return targetRightGain; }

    public float getCurrentLeftGain() { return currentLeftGain; }
    public float getCurrentRightGain() { return currentRightGain; }

    public void setCurrentGains(float left, float right) {
        this.currentLeftGain = left;
        this.currentRightGain = right;
    }

    @Override public void setLooping(boolean looping) { state.setLooping(looping); }
    @Override public void stop() { state.stop(); }
    @Override public void setVolume(float volume) { state.setVolume(volume); }
    @Override public void setPan(float pan) { state.setPan(pan); }
    @Override public void seek(long milliseconds) { state.seek(milliseconds); }
    @Override public void seekFrame(long frame) { state.seekFrame(frame); }
    public PlaybackState getInnerState() { return state; }
    public VkeSpeaker getSpeaker() { return speaker; }
}
package com.vke.api.audio;

import com.vke.core.audio.pcm.EnginePCM;

public class AudioClip {
    private final EnginePCM pcm;

    public AudioClip(EnginePCM pcm) {
        this.pcm = pcm;
    }
}

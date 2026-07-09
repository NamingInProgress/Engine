package com.vke.core.audio;

import com.vke.core.audio.pcm.reader.PCMReader;
import com.vke.core.audio.pcm.reader.SharedPCMReader;
import com.vke.core.audio.source.AudioClip;

public class PreloadedAudioClip implements AudioClip {
    private final PCMReader pcm;

    public PreloadedAudioClip(PCMReader pcm) {
        this.pcm = pcm;
    }

    @Override
    public PCMReader createReader() {
        return new SharedPCMReader(pcm);
    }
}

package com.vke.core.audio.playback.service;

import com.vke.core.audio.playback.Mixer;

public interface AudioManagerMaster {
    void mixer(Mixer mixer);
    void removeMixer(Mixer source);
}

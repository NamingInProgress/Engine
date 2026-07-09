package com.vke.core.audio.playback2d.service;

import com.vke.api.assets.AssetHandle;
import com.vke.api.audio.playback.PlayingAudio;
import com.vke.api.services2.Service;
import com.vke.core.audio.source.AudioClip;
import com.vke.utils.io.Identifier;

public interface AudioManager2D extends Service {
    PlayingAudio play(AudioClip audio);
    PlayingAudio play(AssetHandle<AudioClip> audio);
    PlayingAudio play(Identifier audio);
    PlayingAudio play(String audio);

    void setVolume(float volume);
    float getVolume();
}

package com.vke.core.audio.playback2d.service;

import com.vke.api.assets.AssetHandle;
import com.vke.api.audio.playback.PlayingAudio;
import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.Identifier;
import com.vke.core.audio.source.AudioClip;

public class AudioManager2DAPI extends ServiceAPI implements AudioManager2D {
    public AudioManager2DAPI(ServiceImpl baseImpl) {
        super(baseImpl.getId(), baseImpl);
    }

    private AudioManager2D getImpl() {
        return (AudioManager2D) getImplementation();
    }

    @Override
    public PlayingAudio play(AudioClip audio) {
        return getImpl().play(audio);
    }

    @Override
    public PlayingAudio play(AssetHandle<AudioClip> audio) {
        return getImpl().play(audio);
    }

    @Override
    public PlayingAudio play(Identifier audio) {
        return getImpl().play(audio);
    }

    @Override
    public PlayingAudio play(String audio) {
        return getImpl().play(audio);
    }

    @Override
    public void setVolume(float volume) {
        getImpl().setVolume(volume);
    }

    @Override
    public float getVolume() {
        return getImpl().getVolume();
    }
}

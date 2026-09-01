package com.vke.core.audio.playback2d.service;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.audio.playback.PlayingAudio;
import com.vke.core.Context;
import com.vke.core.Identifier;
import com.vke.core.audio.source.AudioClip;

import java.util.List;

public class AudioManager2DScopedImpl implements AudioManager2D {
    private final Context context;
    private final AudioManager2D base;

    AudioManager2DScopedImpl(Context context, AudioManager2D base) {
        this.context = context;
        this.base = base;
    }

    @Override
    public PlayingAudio play(AudioClip audio) {
        return base.play(audio);
    }

    @Override
    public PlayingAudio play(AssetHandle<AudioClip> audio) {
        return base.play(audio);
    }

    @Override
    public PlayingAudio play(Identifier audio) {
        return play(R.audios.get(audio));
    }

    @Override
    public PlayingAudio play(String audio) {
        return play(context.id(audio));
    }

    @Override
    public void setVolume(float volume) {
        base.setVolume(volume);
    }

    @Override
    public float getVolume() {
        return base.getVolume();
    }

    @Override
    public String getId() {
        return base.getId();
    }

    @Override
    public List<String> dependencies() {
        return base.dependencies();
    }

    @Override
    public void free() {

    }
}

package com.vke.core.audio.playback2d.service;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.audio.playback.PlayingAudio;
import com.vke.api.services2.ScopedServiceImpl;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.audio.pcm.reader.PCMReader;
import com.vke.core.audio.playback.MasterMixer;
import com.vke.core.audio.playback.PlaybackState;
import com.vke.core.audio.playback.service.AudioManagerMaster;
import com.vke.core.audio.playback2d.Mixer2D;
import com.vke.core.audio.source.AudioClip;
import com.vke.core.services2.Services;
import com.vke.utils.io.Identifier;

import java.io.IOException;
import java.util.List;

public class AudioManager2DBaseImpl extends ScopedServiceImpl<AudioManager2DScopedImpl> implements AudioManager2D {
    private Mixer2D mixer;

    public AudioManager2DBaseImpl(VKEngine engine) {
        super(Services.AUDIO_MANAGER_2D, engine);
    }

    @Override
    protected AudioManager2DScopedImpl createScoped(Context context) {
        return new AudioManager2DScopedImpl(context, this);
    }

    @Override
    protected void onInitialize() {
        this.mixer = new Mixer2D(MasterMixer.CHANNELS);
        AudioManagerMaster audioManagerMaster = engine.service(Services.AUDIO_MANAGER_MASTER);
        audioManagerMaster.mixer(mixer);
    }

    private PlayingAudio playInternal(AudioClip audio) {
        PCMReader pcm = audio.createReader();
        PlaybackState state = PlaybackState.fromReader(pcm);
        mixer.request(state);
        return state;
    }

    @Override
    public PlayingAudio play(AudioClip audio) {
        return playInternal(audio);
    }

    @Override
    public PlayingAudio play(AssetHandle<AudioClip> audio) {
        try {
            AudioClip clip = audio.acquire(engine);
            return playInternal(clip);
        } catch (IOException e) {
            //probably ignore for now
            engine.getLogger().error("When playing audio " + audio.getAssetName(), e);
            return null;
        }
    }

    @Override
    public PlayingAudio play(Identifier audio) {
        return play(R.audios.get(audio));
    }

    @Override
    public PlayingAudio play(String audio) {
        return play(engine.id(audio));
    }

    @Override
    public void setVolume(float volume) {
        mixer.setVolume(volume);
    }

    @Override
    public float getVolume() {
        return mixer.getVolume();
    }

    @Override
    public List<String> dependencies() {
        return List.of(Services.AUDIO_MANAGER_MASTER);
    }

    @Override
    public void free() {
        AudioManagerMaster audioManagerMaster = engine.service(Services.AUDIO_MANAGER_MASTER);
        audioManagerMaster.removeMixer(mixer);
    }
}

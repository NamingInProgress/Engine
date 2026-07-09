package com.vke.core.audio.playback3d.service;

import com.vke.api.services2.ScopedServiceImpl;
import com.vke.core.Context;
import com.vke.core.VKEngine;
import com.vke.core.audio.playback.MasterMixer;
import com.vke.core.audio.playback.service.AudioManagerMaster;
import com.vke.core.audio.playback3d.*;
import com.vke.core.services2.Services;

import java.util.List;

public class AudioManager3DBaseImpl extends ScopedServiceImpl<AudioManager3DScopedImpl> implements AudioManager3D {
    private Mixer3D mixer;

    public AudioManager3DBaseImpl(VKEngine engine) {
        super(Services.AUDIO_MANAGER_3D, engine);
    }

    @Override
    protected AudioManager3DScopedImpl createScoped(Context context) {
        return new AudioManager3DScopedImpl(context, this);
    }

    @Override
    protected void onInitialize() {
        this.mixer = new Mixer3D(MasterMixer.CHANNELS);
        AudioManagerMaster audioManagerMaster = engine.service(Services.AUDIO_MANAGER_MASTER);
        audioManagerMaster.mixer(mixer);
    }

    @Override
    public Ear createEar() {
        return new VkeEar();
    }

    @Override
    public Speaker createSpeaker() {
        return createSpeaker(engine);
    }

    @Override
    public Speaker createSpeaker(Context context) {
        return new VkeSpeaker(context, mixer);
    }

    @Override
    public void setListeningEar(Ear ear) {
        mixer.setEar(ear);
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

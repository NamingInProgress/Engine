package com.vke.core.audio.playback.service;

import com.vke.api.services2.ServiceImpl;
import com.vke.core.VKEngine;
import com.vke.core.audio.AudioException;
import com.vke.core.audio.device.AudioDevice;
import com.vke.core.audio.device.JavaAudioDevice;
import com.vke.core.audio.playback.MasterMixer;
import com.vke.core.audio.playback.Mixer;
import com.vke.core.audio.playback.PlaybackController;
import com.vke.core.services2.Services;

import java.util.List;

public class AudioManagerMasterImpl extends ServiceImpl implements AudioManagerMaster {
    private PlaybackController controller;

    public AudioManagerMasterImpl(VKEngine engine) {
        super(Services.AUDIO_MANAGER_MASTER, engine);
    }

    @Override
    protected void onInitialize() {
        try {
            AudioDevice device = new JavaAudioDevice(MasterMixer.CHANNELS);
            this.controller = new PlaybackController(device);
            this.controller.start();
        } catch (AudioException e) {
            engine.throwException(e, "AudioManagerMasterImpl#initialize");
        }
    }

    @Override
    public List<String> dependencies() {
        return List.of();
    }

    @Override
    public void mixer(Mixer mixer) {
        controller.getMixer().addMixer(mixer);
    }

    @Override
    public void removeMixer(Mixer mixer) {
        controller.getMixer().removeMixer(mixer);
    }

    @Override
    public void free() {
        controller.free();
    }
}

package com.vke.core.audio.playback.service;

import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.audio.playback.Mixer;

public class AudioManagerMasterAPI extends ServiceAPI implements AudioManagerMaster {
    public AudioManagerMasterAPI(ServiceImpl baseImpl) {
        super(baseImpl.getId(), baseImpl);
    }

    private AudioManagerMaster getImpl() {
        return (AudioManagerMaster) getImplementation();
    }

    @Override
    public void mixer(Mixer mixer) {
        getImpl().mixer(mixer);
    }

    @Override
    public void removeMixer(Mixer source) {
        getImpl().removeMixer(source);
    }
}

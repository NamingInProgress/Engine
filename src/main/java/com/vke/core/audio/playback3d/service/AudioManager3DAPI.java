package com.vke.core.audio.playback3d.service;

import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.Context;
import com.vke.core.audio.playback3d.Ear;
import com.vke.core.audio.playback3d.Speaker;

public class AudioManager3DAPI extends ServiceAPI implements AudioManager3D {
    public AudioManager3DAPI(ServiceImpl baseImpl) {
        super(baseImpl.getId(), baseImpl);
    }

    private AudioManager3D getImpl() {
        return (AudioManager3D) getImplementation();
    }

    @Override
    public Ear createEar() {
        return getImpl().createEar();
    }

    @Override
    public Speaker createSpeaker() {
        return getImpl().createSpeaker();
    }

    @Override
    public Speaker createSpeaker(Context context) {
        return getImpl().createSpeaker(context);
    }

    @Override
    public void setListeningEar(Ear ear) {
        getImpl().setListeningEar(ear);
    }
}

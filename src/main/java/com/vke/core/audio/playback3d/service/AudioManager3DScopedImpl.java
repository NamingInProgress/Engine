package com.vke.core.audio.playback3d.service;

import com.vke.core.Context;
import com.vke.core.audio.playback3d.Ear;
import com.vke.core.audio.playback3d.Speaker;

import java.util.List;

public class AudioManager3DScopedImpl implements AudioManager3D {
    private final Context context;
    private final AudioManager3D base;

    public AudioManager3DScopedImpl(Context context, AudioManager3D base) {
        this.context = context;
        this.base = base;
    }

    @Override
    public Ear createEar() {
        return base.createEar();
    }

    @Override
    public Speaker createSpeaker() {
        return base.createSpeaker(context);
    }

    @Override
    public Speaker createSpeaker(Context context) {
        return base.createSpeaker(context);
    }

    @Override
    public void setListeningEar(Ear ear) {
        base.setListeningEar(ear);
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

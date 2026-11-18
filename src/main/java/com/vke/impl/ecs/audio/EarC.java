package com.vke.impl.ecs.audio;

import com.vke.core.audio.playback3d.Ear;
import com.vke.core.audio.playback3d.service.AudioManager3D;
import com.vke.core.ecs.component.Component;
import pl.epsi.EcsComponent;

@EcsComponent
public class EarC implements Component {
    public Ear[] ear;

    @Override
    public void initialize(int i) {

    }

    public void createEar(int i, AudioManager3D audioManager3D) {
        ear[i] = audioManager3D.createEar();
    }
}

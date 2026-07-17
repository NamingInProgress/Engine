package com.vke.core.audio.playback3d;

import com.vke.utils.types.AbstractPositionable;
import org.joml.Quaternionf;

public class VkeEar extends AbstractPositionable implements Ear {
    private Quaternionf rotation;

    public VkeEar() {
        this.rotation = new Quaternionf();
    }

    @Override
    public void setRotation(Quaternionf rotation) {
        this.rotation = rotation;
    }

    @Override
    public Quaternionf getRotation() {
        return rotation;
    }
}

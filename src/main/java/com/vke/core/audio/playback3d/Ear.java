package com.vke.core.audio.playback3d;

import org.joml.Quaternionf;

public interface Ear extends Positionable {
    void setRotation(Quaternionf rotation);
    Quaternionf getRotation();
}

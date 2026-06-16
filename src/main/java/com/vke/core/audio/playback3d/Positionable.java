package com.vke.core.audio.playback3d;

import org.joml.Vector3f;

public interface Positionable {
    Vector3f getPosition();
    void setPosition(Vector3f position);
}

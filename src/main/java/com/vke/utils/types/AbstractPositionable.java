package com.vke.utils.types;

import com.vke.core.audio.playback3d.Positionable;
import org.joml.Vector3f;

public class AbstractPositionable implements Positionable {
    protected Vector3f position = new Vector3f();

    @Override
    public Vector3f getPosition() {
        return position;
    }

    @Override
    public void setPosition(Vector3f position) {
        this.position = position;
    }
}

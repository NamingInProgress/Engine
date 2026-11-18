package com.vke.core.audio.playback3d;

import com.vke.core.game.object.GameObjectTransform;

public class VkeEar implements Ear {
    private GameObjectTransform transform;

    public VkeEar() {

    }

    @Override
    public void setTransform(GameObjectTransform transform) {
        this.transform = transform;
    }

    @Override
    public GameObjectTransform getTransform() {
        return transform;
    }
}

package com.vke.core.audio.playback3d;

import com.vke.core.game.object.GameObjectTransform;

public interface SpatialAudioComponent {
    void setTransform(GameObjectTransform transform);
    GameObjectTransform getTransform();
}

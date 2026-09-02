package com.vke.core.game.object.controller;

import com.vke.api.framable.Framable;
import com.vke.core.game.object.GameObject;

public interface GameObjectController extends Framable {
    GameObject getAttachedObject();
    void setAttachedObject(GameObject object);
}

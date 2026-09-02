package com.vke.core.game.object.controller;

import com.vke.core.Context;
import com.vke.core.framable.service.FramableManager;
import com.vke.core.game.object.GameObject;
import com.vke.core.services2.Services;

public class AbstractGameObjectController implements GameObjectController{
    protected Context context;
    protected GameObject gameObject;
    protected boolean attached;

    private final FramableManager framableManager;

    public AbstractGameObjectController(Context context) {
        this.context = context;
        this.framableManager = context.service(Services.FRAMABLE_MANAGER);
    }

    @Override
    public GameObject getAttachedObject() {
        return gameObject;
    }

    @Override
    public void setAttachedObject(GameObject object) {
        if (object == null) {
            if (attached) {
                framableManager.removeFramable(this);
                attached = false;
            }
        } else {
            if (!attached) {
                framableManager.registerFramable(this);
                attached = true;
            }
        }
        this.gameObject = object;
    }
}

package com.vke.impl.gameobject;

import com.vke.core.Context;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.core.game.object.AbstractGameObject;
import com.vke.core.game.object.GameObject;

public class EmptyGameObject extends AbstractGameObject {

    public EmptyGameObject(Context ctx) {
        super(ctx);
    }

    @Override
    protected ComponentMask createMask() {
        return ComponentMask.of();
    }

    @Override
    protected GameObject createFromSpawnedEntity(int entity) {
        return new EmptyGameObject(ctx);
    }

    @Override
    public void onSpawned() {}
}

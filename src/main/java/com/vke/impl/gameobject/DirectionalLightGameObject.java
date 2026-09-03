package com.vke.impl.gameobject;

import com.vke.core.Context;
import com.vke.core.color.RgbColor;
import com.vke.core.ecs.ComponentReference;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.core.game.object.AbstractGameObject;
import com.vke.core.game.object.GameObject;
import com.vke.core.game.object.RestrictedGameObject;
import com.vke.impl.ecs.TransformC;
import com.vke.impl.ecs.WorldTransformC;
import com.vke.impl.ecs.light.DirectionalLightC;

public class DirectionalLightGameObject extends AbstractGameObject implements RestrictedGameObject {
    private ComponentReference<DirectionalLightC> directional;

    public DirectionalLightGameObject(Context ctx) {
        super(ctx);
    }

    public void setColor(RgbColor color) {
        setColor(color.r(), color.g(), color.b());
    }

    public void setColor(float r, float g, float b) {
        requireSpawned();
        DirectionalLightC c = directional.getComponent();
        int i = directional.getIndex();
        c.r[i] = r;
        c.g[i] = g;
        c.b[i] = b;
    }

    public void setIntensity(float intensity) {
        requireSpawned();
        directional.getComponent().intensity[directional.getIndex()] = intensity;
    }

    @Override
    protected ComponentMask createMask() {
        return new ComponentMask(DirectionalLightC.ID, TransformC.ID, WorldTransformC.ID);
    }

    @Override
    public void onSpawned() {
        directional = getComponent(DirectionalLightC.ID);
    }

    @Override
    protected GameObject createFromSpawnedEntity(int entity) {
        DirectionalLightGameObject go = new DirectionalLightGameObject(ctx);
        go.entityId = entity;
        return go;
    }

    @Override
    public int[] getFixedComponents() {
        return new int[]{ DirectionalLightC.ID };
    }
}

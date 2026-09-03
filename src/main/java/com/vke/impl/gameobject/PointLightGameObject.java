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
import com.vke.impl.ecs.light.PointLightC;

public class PointLightGameObject extends AbstractGameObject implements RestrictedGameObject {
    private ComponentReference<PointLightC> point;

    public PointLightGameObject(Context ctx) {
        super(ctx);
    }

    public void setColor(RgbColor color) {
        setColor(color.r(), color.g(), color.b());
    }

    public void setColor(float r, float g, float b) {
        requireSpawned();
        PointLightC c = point.getComponent();
        int i = point.getIndex();
        c.r[i] = r;
        c.g[i] = g;
        c.b[i] = b;
    }

    public void setIntensity(float intensity) {
        setIntensity(intensity, true);
    }

    public void setIntensity(float intensity, boolean autoRange) {
        requireSpawned();
        point.getComponent().intensity[point.getIndex()] = intensity;
        if (autoRange) {
            autoSetRange();
        }
    }

    public void autoSetRange() {
        requireSpawned();
        PointLightC c = point.getComponent();
        int i = point.getIndex();
        c.range[i] = c.autoRange(c.intensity[i]);
    }

    @Override
    protected ComponentMask createMask() {
        return new ComponentMask(PointLightC.ID, TransformC.ID, WorldTransformC.ID);
    }

    @Override
    public void onSpawned() {
        point = getComponent(PointLightC.ID);
    }

    @Override
    protected GameObject createFromSpawnedEntity(int entity) {
        PointLightGameObject go = new PointLightGameObject(ctx);
        go.entityId = entity;
        return go;
    }

    @Override
    public int[] getFixedComponents() {
        return new int[]{ PointLightC.ID };
    }

}

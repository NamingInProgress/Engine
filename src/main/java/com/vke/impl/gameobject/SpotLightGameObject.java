package com.vke.impl.gameobject;

import com.vke.api.annotation.InDegrees;
import com.vke.core.Context;
import com.vke.core.color.RgbColor;
import com.vke.core.ecs.ComponentReference;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.core.game.object.AbstractGameObject;
import com.vke.core.game.object.GameObject;
import com.vke.core.game.object.RestrictedGameObject;
import com.vke.impl.ecs.TransformC;
import com.vke.impl.ecs.WorldTransformC;
import com.vke.impl.ecs.light.SpotLightC;

public class SpotLightGameObject extends AbstractGameObject implements RestrictedGameObject {
    private ComponentReference<SpotLightC> spotC;

    public SpotLightGameObject(Context ctx) {
        super(ctx);
    }

    public void setColor(RgbColor color) {
        setColor(color.r(), color.g(), color.b());
    }

    public void setColor(float r, float g, float b) {
        requireSpawned();
        SpotLightC c = spotC.getComponent();
        int i = spotC.getIndex();
        c.r[i] = r;
        c.g[i] = g;
        c.b[i] = b;
    }

    public void setIntensity(float intensity) {
        setIntensity(intensity, true);
    }

    public void setIntensity(float intensity, boolean autoRange) {
        requireSpawned();
        spotC.getComponent().intensity[spotC.getIndex()] = intensity;
        if (autoRange) {
            autoSetRange();
        }
    }

    public void autoSetRange() {
        requireSpawned();
        SpotLightC c = spotC.getComponent();
        int i = spotC.getIndex();
        c.range[i] = c.autoRange(c.intensity[i]);
    }

    public void setRange(float range) {
        requireSpawned();
        spotC.getComponent().range[spotC.getIndex()] = range;
    }

    public void setInnerConeAngle(@InDegrees float angle) {
        requireSpawned();
        spotC.getComponent().innerConeCos[spotC.getIndex()] = (float) Math.cos(Math.toRadians(angle));
    }

    public void setOuterConeAngle(@InDegrees float angle) {
        requireSpawned();
        spotC.getComponent().outerConeCos[spotC.getIndex()] = (float) Math.cos(Math.toRadians(angle));
    }

    @Override
    protected ComponentMask createMask() {
        return new ComponentMask(SpotLightC.ID, TransformC.ID, WorldTransformC.ID);
    }

    @Override
    public void onSpawned() {
        spotC = getComponent(SpotLightC.ID);
    }

    @Override
    protected GameObject createFromSpawnedEntity(int entity) {
        SpotLightGameObject go = new SpotLightGameObject(ctx);
        go.entityId = entity;
        return go;
    }

    @Override
    public int[] getFixedComponents() {
        return new int[]{ SpotLightC.ID };
    }
}

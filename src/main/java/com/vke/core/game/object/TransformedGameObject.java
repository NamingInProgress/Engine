package com.vke.core.game.object;

import com.vke.core.ecs.ComponentReference;
import com.vke.impl.ecs.TransformC;
import org.joml.Quaternionf;

public interface TransformedGameObject extends RestrictedGameObject {
    default ComponentReference<TransformC> transformComponent() {
        return getComponent(TransformC.ID);
    }

    @Override
    default int[] getFixedComponents() {
        return new int[] { TransformC.ID };
    }

    private TransformC c() {
        return transformComponent().getComponent();
    }

    private int i() {
        return transformComponent().getIndex();
    }

    default void setX(float x) {
        c().x[i()] = x;
    }

    default void changeX(float dx) {
        c().x[i()] += dx;
    }

    default float getX() { return c().x[i()]; }
    default float getY() { return c().y[i()]; }
    default float getZ() { return c().z[i()]; }

    default Quaternionf getRotation() {
        return c().rotation(i());
    }
}

package com.vke.core.game.object;

import com.vke.core.ecs.ComponentReference;
import com.vke.core.ecs.component.Component;
import com.vke.core.ecs.component.mask.ComponentMask;

public interface GameObject {
    default boolean isSpawned() {
        return entityId() != -1;
    }

    int entityId();
    void spawn();
    void destroy();
    GameObject duplicate();

    GameObject[] spawnBatch(int num);

    void addComponents(int... componentId);
    void removeComponents(int... componentId);

    <T extends Component> ComponentReference<T> getComponent(int id);
    ComponentMask components();
}

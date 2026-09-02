package com.vke.core.ecs.services;

import com.vke.api.services2.PinnedService;
import com.vke.core.ecs.ComponentReference;
import com.vke.core.ecs.api.EntityInitializer;
import com.vke.core.ecs.api.EntityTransitionInitializer;
import com.vke.core.ecs.api.Query;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.component.Component;
import com.vke.core.ecs.component.mask.ComponentMask;
import org.jetbrains.annotations.Nullable;

public interface EcsManager extends PinnedService {
    int[] spawnEntities(int amount, ComponentMask mask, @Nullable EntityInitializer initializer);
    void destroyEntity(int entity);
    void destroyEntities(int[] entities, int start, int length);
    default void destroyEntities(int[] entities) {
        destroyEntities(entities, 0, entities.length);
    }
    void transitionEntity(int entity, ComponentMask newMask, @Nullable EntityTransitionInitializer initializer);
    int duplicateEntity(int entity);
    int createCategory();
    void registerQuery(int category, Query query);
    long runQueries(int category);
    <T extends Component> ComponentReference<T> obtainComponentReference(int entity, int componentId);
    String getComponentName(int id);
    EntityLocation locateEntity(int entity);

    record EntityLocation(Archetype archetype, int index) {
    }
}

package com.vke.core.ecs.services;

import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import com.vke.core.ecs.ComponentReference;
import com.vke.core.ecs.api.EntityInitializer;
import com.vke.core.ecs.api.EntityTransitionInitializer;
import com.vke.core.ecs.api.Query;
import com.vke.core.ecs.component.Component;
import com.vke.core.ecs.component.mask.ComponentMask;
import org.jetbrains.annotations.Nullable;

public class EcsManagerAPI extends ServiceAPI implements EcsManager {

    public EcsManagerAPI(ServiceImpl baseImpl) {
        super(baseImpl.getId(), baseImpl);
    }

    private EcsManager getImpl() {
        return (EcsManager) getImplementation();
    }

    @Override
    public int[] spawnEntities(int amount, ComponentMask mask, @Nullable EntityInitializer initializer) {
        return getImpl().spawnEntities(amount, mask, initializer);
    }

    @Override
    public void destroyEntity(int entity) {
        getImpl().destroyEntity(entity);
    }

    @Override
    public void destroyEntities(int[] entities) {
        getImpl().destroyEntities(entities);
    }

    @Override
    public void transitionEntity(int entity, ComponentMask newMask, @Nullable EntityTransitionInitializer initializer) {
        getImpl().transitionEntity(entity, newMask, initializer);
    }

    @Override
    public int duplicateEntity(int entity) {
        return getImpl().duplicateEntity(entity);
    }

    @Override
    public int createCategory() {
        return getImpl().createCategory();
    }

    @Override
    public void registerQuery(int category, Query query) {
        getImpl().registerQuery(category, query);
    }

    @Override
    public long runQueries(int category) {
        return getImpl().runQueries(category);
    }

    @Override
    public <T extends Component> ComponentReference<T> obtainComponentReference(int entity, int componentId) {
        return getImpl().obtainComponentReference(entity, componentId);
    }

    @Override
    public String getComponentName(int componentId) {
        return getImpl().getComponentName(componentId);
    }

    @Override
    public EntityLocation locateEntity(int entity) {
        return getImpl().locateEntity(entity);
    }
}

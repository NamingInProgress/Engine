package com.vke.core.ecs.services;

import com.vke.api.services2.ServiceImpl;
import com.vke.core.VKEngine;
import com.vke.core.ecs.ComponentReference;
import com.vke.core.ecs.EcsCreateInfo;
import com.vke.core.ecs.api.EntityInitializer;
import com.vke.core.ecs.api.EntityTransitionInitializer;
import com.vke.core.ecs.api.Query;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.backend.ArchetypeManager;
import com.vke.core.ecs.backend.ComponentRegistry;
import com.vke.core.ecs.backend.EntityAllocator;
import com.vke.core.ecs.backend.query.QueryManager;
import com.vke.core.ecs.component.Component;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.core.services2.Services;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EcsManagerImpl extends ServiceImpl implements EcsManager {
    private EcsCreateInfo createInfo;
    private EntityAllocator entityAllocator;
    private ArchetypeManager archetypeManager;
    private QueryManager queryManager;
    private int nextCategory;


    public EcsManagerImpl(VKEngine engine) {
        super(Services.ECS, engine);
    }

    @Override
    public void onInitialize() {
        this.createInfo = new EcsCreateInfo();
        this.entityAllocator = new EntityAllocator();
        int allComponents = ComponentRegistry.getCOUNTER();
        int usedComponents = createInfo.usedComponentCount >= 0 ? createInfo.usedComponentCount : allComponents;
        this.queryManager = new QueryManager();
        this.archetypeManager = new ArchetypeManager(entityAllocator, usedComponents, queryManager);
        this.entityAllocator.setArchetypeManager(archetypeManager);
    }

    @Override
    public int[] spawnEntities(int amount, ComponentMask mask, @Nullable EntityInitializer initializer) {
        Archetype archetype = archetypeManager.acquireArchetype(mask);
        return archetype.spawnEntities(amount, initializer, entityAllocator, queryManager);
    }

    @Override
    public void destroyEntity(int entity) {
        archetypeManager.destroyEntity(entity);
    }

    @Override
    public void destroyEntities(int[] entities, int start, int length) {
        archetypeManager.destroyEntities(entities, start, length);
    }

    @Override
    public void transitionEntity(int entity, ComponentMask newMask, @Nullable EntityTransitionInitializer initializer) {
        archetypeManager.transitionEntity(entity, newMask, initializer);
    }

    @Override
    public int duplicateEntity(int entity) {
        return archetypeManager.duplicateEntity(entity);
    }

    @Override
    public int createCategory() {
        return nextCategory++;
    }

    @Override
    public void registerQuery(int category, Query query) {
        queryManager.registerQuery(category, query);
    }

    @Override
    public long runQueries(int category) {
        return queryManager.runCategory(category);
    }

    @Override
    public List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {

    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Component> ComponentReference<T> obtainComponentReference(int entity, int componentId) {
        return (ComponentReference<T>) archetypeManager.obtainCompRef(entity, componentId);
    }

    @Override
    public String getComponentName(int compId) {
        return ComponentRegistry.getInstance(compId).getClass().getSimpleName();
    }

    @Override
    public EntityLocation locateEntity(int entity) {
        Archetype at = entityAllocator.getArchetype(entity);
        int idx = entityAllocator.getArchetypeIndex(entity);
        return new EntityLocation(at, idx);
    }
}

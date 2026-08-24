package com.vke.core.ecs;

import com.vke.core.ecs.api.EntityInitializer;
import com.vke.core.ecs.api.EntityTransitionInitializer;
import com.vke.core.ecs.api.Query;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.backend.ArchetypeManager;
import com.vke.core.ecs.backend.ComponentRegistry;
import com.vke.core.ecs.backend.EntityAllocator;
import com.vke.core.ecs.backend.query.QueryManager;
import com.vke.core.ecs.component.Component;
import com.vke.core.ecs.component.TestComponent;
import com.vke.core.ecs.component.mask.ComponentMask;
import org.jetbrains.annotations.Nullable;

public class Ecs {
    private final EcsCreateInfo createInfo;
    private final EntityAllocator entityAllocator;
    private final ArchetypeManager archetypeManager;
    private final QueryManager queryManager;
    private int nextCategory;


    public Ecs(EcsCreateInfo createInfo) {
        this.createInfo = createInfo;
        this.entityAllocator = new EntityAllocator();
        int allComponents = ComponentRegistry.getCOUNTER();
        int usedComponents = createInfo.usedComponentCount >= 0 ? createInfo.usedComponentCount : allComponents;
        this.queryManager = new QueryManager();
        this.archetypeManager = new ArchetypeManager(entityAllocator, usedComponents, queryManager);
        this.entityAllocator.setArchetypeManager(archetypeManager);
    }

    public int[] spawnEntities(int amount, ComponentMask mask, @Nullable EntityInitializer initializer) {
        Archetype archetype = archetypeManager.acquireArchetype(mask);
        return archetype.spawnEntities(amount, initializer, entityAllocator, queryManager);
    }

    public void destroyEntity(int entity) {
        archetypeManager.destroyEntity(entity);
    }

    public void destroyEntities(int[] entities) {
        archetypeManager.destroyEntities(entities);
    }

    public void transitionEntity(int entity, ComponentMask newMask, @Nullable EntityTransitionInitializer initializer) {
        archetypeManager.transitionEntity(entity, newMask, initializer);
    }

    public int createCategory() {
        return nextCategory++;
    }

    public void registerQuery(int category, Query query) {
        queryManager.registerQuery(category, query);
    }

    public void runQueries(int category) {
        queryManager.runCategory(category);
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> ComponentReference<T> createComponentReference(int entity, int componentId) {
        return (ComponentReference<T>) archetypeManager.createCompRef(entity, componentId);
    }
}

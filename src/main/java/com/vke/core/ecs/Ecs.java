package com.vke.core.ecs;

import com.vke.core.ecs.api.EntityInitializer;
import com.vke.core.ecs.api.EntityTransitionInitializer;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.backend.ArchetypeManager;
import com.vke.core.ecs.backend.ComponentRegistry;
import com.vke.core.ecs.backend.EntityAllocator;
import com.vke.core.ecs.component.TestComponent;
import com.vke.core.ecs.component.mask.ComponentMask;
import org.jetbrains.annotations.Nullable;

public class Ecs {
    private final EcsCreateInfo createInfo;
    private final EntityAllocator entityAllocator;
    private final ArchetypeManager archetypeManager;


    protected Ecs(EcsCreateInfo createInfo) {
        this.createInfo = createInfo;
        this.entityAllocator = new EntityAllocator();
        int allComponents = ComponentRegistry.getCOUNTER();
        int usedComponents = createInfo.usedComponentCount >= 0 ? createInfo.usedComponentCount : allComponents;
        this.archetypeManager = new ArchetypeManager(entityAllocator, usedComponents);
    }

    public int[] spawnEntities(int amount, ComponentMask mask, @Nullable EntityInitializer initializer) {
        Archetype archetype = archetypeManager.acquireArchetype(mask);
        return archetype.spawnEntities(amount, initializer, entityAllocator);
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
}

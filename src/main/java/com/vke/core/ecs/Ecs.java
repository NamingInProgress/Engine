package com.vke.core.ecs;

import com.vke.core.ecs.api.EntityInitializer;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.backend.ArchetypeManager;
import com.vke.core.ecs.backend.EntityAllocator;
import com.vke.core.ecs.component.mask.ComponentMask;

public class Ecs {
    private static int COMPONENT_IDX;
    private final EcsCreateInfo createInfo;
    private final EntityAllocator entityAllocator;
    private final ArchetypeManager archetypeManager = null;


    protected Ecs(EcsCreateInfo createInfo) {
        this.createInfo = createInfo;
        this.entityAllocator = new EntityAllocator();
        int usedComponents = createInfo.usedComponentCount >= 0 ? createInfo.usedComponentCount : COMPONENT_IDX;
        //this.archetypeManager = new ArchetypeManager(entityAllocator, usedComponents);
    }

    public int[] spawnEntities(int amount, ComponentMask mask, EntityInitializer initializer) {
        Archetype archetype = archetypeManager.acquireArchetype(mask);
        return archetype.spawnEntities(amount, initializer, entityAllocator);
    }
}

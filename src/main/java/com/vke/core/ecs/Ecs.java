package com.vke.core.ecs;

import com.vke.core.ecs.api.EntityInitializer;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.backend.EntityAllocator;
import com.vke.core.ecs.component.mask.ComponentMask;

public class Ecs {
    private final EcsCreateInfo createInfo;
    private final EntityAllocator entityAllocator;


    protected Ecs(EcsCreateInfo createInfo) {
        this.createInfo = createInfo;
        this.entityAllocator = new EntityAllocator();
    }

    public int[] spawnEntities(int amount, ComponentMask mask, EntityInitializer initializer) {
        Archetype archetype = null;
        archetype.spawnEntities(amount, initializer, entityAllocator);
        return null;
    }
}

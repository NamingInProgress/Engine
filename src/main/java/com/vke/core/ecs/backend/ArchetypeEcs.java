package com.vke.core.ecs.backend;

import com.vke.core.ecs.Ecs;
import com.vke.core.ecs.EcsCreateInfo;

public class ArchetypeEcs extends Ecs {
    public ArchetypeEcs(EcsCreateInfo createInfo) {
        super(createInfo);
    }

    @Override
    public long[] spawnEntities(int amount) {
        return new long[0];
    }
}

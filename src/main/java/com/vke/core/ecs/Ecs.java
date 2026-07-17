package com.vke.core.ecs;

public abstract class Ecs {
    protected final EcsCreateInfo createInfo;

    protected Ecs(EcsCreateInfo createInfo) {
        this.createInfo = createInfo;
    }

    public abstract long[] spawnEntities(int amount, );
}

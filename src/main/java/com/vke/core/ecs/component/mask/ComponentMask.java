package com.vke.core.ecs.component.mask;

public sealed interface ComponentMask permits U64ComponentMask, U128ComponentMask, WideComponentMask {
    boolean contains(ComponentMask other);
    ComponentMask extend(ComponentMask mask);

    long fastHash();
    boolean fastEquals(ComponentMask other);
}

package com.vke.core.ecs;

public interface ComponentMask {
    boolean contains(ComponentMask other);
    ComponentMask extend(ComponentMask mask);
}

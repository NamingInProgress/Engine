package com.vke.core.ecs.api;

import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.component.mask.ComponentMask;

public interface Query {
    ComponentMask getMask();
    void execute(Archetype at, int i0, int i1);
}

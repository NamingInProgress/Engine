package com.vke.core.ecs.api;

import com.vke.core.ecs.backend.Archetype;

public interface EntityTransitionInitializer {
    void initialize(Archetype archetype, int index);
}

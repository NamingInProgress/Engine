package com.vke.core.ecs.api;

import com.vke.core.ecs.backend.Archetype;

public interface EntityInitializer {
    void initialize(Archetype archetype, int left, int right, int entityIndex);
}

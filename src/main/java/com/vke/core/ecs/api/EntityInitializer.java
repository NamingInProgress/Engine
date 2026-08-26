package com.vke.core.ecs.api;

import com.vke.core.ecs.backend.Archetype;

@FunctionalInterface
public interface EntityInitializer {
    void initialize(Archetype archetype, int left, int right, int entityIndex, int entityId);
}

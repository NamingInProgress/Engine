package com.vke.core.ecs;

public class EcsCreateInfo {
    public boolean canUseManyComponents = true;
    public BackendType backendType = BackendType.Archetype;

    public enum BackendType {
        SparseArray,
        Archetype
    }
}

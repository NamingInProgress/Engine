package com.vke.core.ecs.backend;

import com.vke.core.ecs.component.mask.ComponentMask;

public class ArchetypeManager {
    private final MaskMap map;

    public ArchetypeManager() {
        this.map = new MaskMap();
    }

    public Archetype acquire(ComponentMask mask) {
        return map.findOrMake(mask, () -> new Archetype(mask));
    }

    /*
    open addressed hash table with some sort of sorted keys so that i can perform binary search probing
    the insert can be 20000 times slower than find, but find has to be blazingly fast
     */
}

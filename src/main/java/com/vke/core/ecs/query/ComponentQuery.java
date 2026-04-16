package com.vke.core.ecs.query;

import com.carrotsearch.hppc.BitSet;
import com.vke.core.ecs.Archetype;

public abstract class ComponentQuery {
    private final BitSet componentMask;

    public ComponentQuery(BitSet componentMask) {
        this.componentMask = componentMask;
    }

    public abstract void execute(Archetype archetype, int from, int to);
}

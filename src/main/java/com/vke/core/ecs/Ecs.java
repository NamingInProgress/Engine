package com.vke.core.ecs;

<<<<<<< HEAD
import com.carrotsearch.hppc.BitSet;
import com.vke.core.ecs.query.ComponentQueryEntry;

import java.util.ArrayList;
import java.util.HashMap;

public class Ecs {
    private HashMap<BitSet, Archetype> archetypes;
    private ArrayList<ComponentQueryEntry> componentQueryEntries;

    public Ecs() {
        archetypes = new HashMap<>();
        componentQueryEntries = new ArrayList<>();
    }

    public long[] spawnEntities(int count, BitSet componentMask) {

    }
=======
public abstract class Ecs {
    protected final EcsCreateInfo createInfo;

    protected Ecs(EcsCreateInfo createInfo) {
        this.createInfo = createInfo;
    }

    public abstract long[] spawnEntities(int amount, );
>>>>>>> origin/ecs
}

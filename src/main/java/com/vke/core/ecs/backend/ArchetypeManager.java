package com.vke.core.ecs.backend;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.vke.core.ecs.api.EntityTransitionInitializer;
import com.vke.core.ecs.component.Component;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.utils.tuple.Ntel;

import java.util.ArrayList;
import java.util.HashMap;

public class ArchetypeManager {
    private final MaskMap map;
    private final EntityAllocator alloc;

    public ArchetypeManager(EntityAllocator alloc, int usedComponents, ComponentRegistry registry) {
        this.map = new MaskMap(usedComponents, registry);
        this.alloc = alloc;
    }

    public Archetype acquireArchetype(ComponentMask mask) {
        return map.findOrMake(mask);
    }

    public void transitionEntity(int entity, ComponentMask newMask, EntityTransitionInitializer initializer){
        Archetype oldArch = alloc.getArchetype(entity);
        int oldIdx = alloc.getArchetypeIndex(entity);

        Archetype newArch = acquireArchetype(newMask);
        int newIdx = newArch.accomodateDangling(entity);

        Component[] oldComps = oldArch.getComponents();

        for (Component oldComp : oldComps) {
            Component newComp  = newArch.getComponentById(oldComp.getId());
            if (newComp != null) {
                newComp.copyFrom(oldComp, oldIdx, newIdx);
            }
        }

        initializer.initialize(newArch, newIdx);

        oldArch.dangleEntity(oldIdx, alloc);
        alloc.setArchetypeIndex(entity, newIdx);
        alloc.setArchetype(entity, newArch);
    }

    public void destroyEntity(int entity) {
        Archetype arch = alloc.getArchetype(entity);
        int index = alloc.getArchetypeIndex(entity);
        arch.destroyEntity(index, alloc);
    }

    private static final int BATCHED_PATH_THRESHOLD = 128;

    public void destroyEntities(int[] entities) {
        if (entities.length < BATCHED_PATH_THRESHOLD) {
            for (int entity : entities) {
                destroyEntity(entity);
            }
        } else {
            int maxArch = Archetype.IDS;
            int educatedGuess = Math.min(entities.length / 50, Math.min(50, maxArch));
            IntObjectHashMap<IntArrayList> byArch = new IntObjectHashMap<>(educatedGuess);
        }
    }
}

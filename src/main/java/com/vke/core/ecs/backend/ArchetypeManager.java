package com.vke.core.ecs.backend;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.ObjectArrayList;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.vke.core.ecs.api.EntityTransitionInitializer;
import com.vke.core.ecs.component.Component;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.core.rendering.vertexconsumer.InstantResetIntArrayList;
import com.vke.core.rendering.vertexconsumer.RecyclerArrayList;
import com.vke.utils.tuple.Ntel;

import java.util.ArrayList;
import java.util.HashMap;

public class ArchetypeManager {
    private final MaskMap map;
    private final EntityAllocator alloc;

    public ArchetypeManager(EntityAllocator alloc, int usedComponents) {
        this.map = new MaskMap(usedComponents);

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

        if (initializer != null) {
            initializer.initialize(newArch, newIdx);
        }

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
            for (int entity : entities) {
                Archetype arch = alloc.getArchetype(entity);
                int index = alloc.getArchetypeIndex(entity);
                int archId = arch.getId();
                IntArrayList slot = byArch.get(archId);
                if (slot == null) {
                    slot = new IntArrayList();
                    byArch.put(archId, slot);
                }

                slot.add(index);
            }

            for (IntObjectCursor<IntArrayList> cursor : byArch) {
                Archetype arch = Archetype.findById(cursor.key);
                IntArrayList list = cursor.value;
                list.sort();

                int size = list.size();
                if (size == 0) continue;

                int i = size - 1;
                while (i >= 0) {
                    int batchEnd = list.buffer[i];
                    int batchStart = batchEnd;
                    int batchSize = 1;

                    while (i - 1 >= 0 && list.buffer[i - 1] == batchStart - 1) {
                        i--;
                        batchStart--;
                        batchSize++;
                    }

                    arch.destroyConsecutiveEntities(batchStart, batchSize, alloc);

                    i--;
                }
            }
        }
    }
}

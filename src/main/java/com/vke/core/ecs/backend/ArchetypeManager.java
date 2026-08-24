package com.vke.core.ecs.backend;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.ObjectArrayList;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.vke.core.ecs.ComponentReference;
import com.vke.core.ecs.api.EntityTransitionInitializer;
import com.vke.core.ecs.backend.query.QueryManager;
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
    private final QueryManager qm;
    private final IntObjectHashMap<IntObjectHashMap<ComponentReference<?>>> references;

    public ArchetypeManager(EntityAllocator alloc, int usedComponents, QueryManager qm) {
        this.map = new MaskMap(usedComponents);

        this.alloc = alloc;
        this.qm = qm;

        this.references = new IntObjectHashMap<>();
    }

    public Archetype acquireArchetype(ComponentMask mask) {
        return map.findOrMake(mask);
    }

    public void transitionEntity(int entity, ComponentMask newMask, EntityTransitionInitializer initializer){
        Archetype oldArch = alloc.getArchetype(entity);
        int oldIdx = alloc.getArchetypeIndex(entity);

        Archetype newArch = acquireArchetype(newMask);
        int newIdx = newArch.accomodateDangling(entity);

        qm.onEntityTransitionOut(oldArch);
        qm.onEntityTransitionIn(newMask, newArch, newIdx);

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

        IntObjectHashMap<ComponentReference<?>> refs = references.get(entity);
        if (refs != null) {
            Archetype at = alloc.getArchetype(entity);
            int i = alloc.getArchetypeIndex(entity);
            for (var o : refs) {
                o.value.__0(at.getComponentById(o.value.__2()));
                o.value.__1(i);
            }
        }
    }

    private void cleanupRefsForEntity(int entity) {
        IntObjectHashMap<ComponentReference<?>> refs = references.get(entity);
        if (refs != null) {
            for (var o : refs) {
                o.value.__0(null);
            }

            references.remove(entity);
        }
    }

    public void destroyEntity(int entity) {
        Archetype arch = alloc.getArchetype(entity);
        int index = alloc.getArchetypeIndex(entity);
        arch.destroyEntity(index, alloc, qm);
        cleanupRefsForEntity(entity);
    }

    private static final int BATCHED_PATH_THRESHOLD = 128;

    public void destroyEntities(int[] entities) {
        if (entities.length < BATCHED_PATH_THRESHOLD) {
            for (int entity : entities) {
                destroyEntity(entity);
                cleanupRefsForEntity(entity);
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

                cleanupRefsForEntity(entity);
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

                    arch.destroyConsecutiveEntities(batchStart, batchSize, alloc, qm);

                    i--;
                }
            }
        }
    }

    public ComponentReference<? extends Component> createCompRef(int entity, int componentId) {
        ComponentReference<?> ref = new ComponentReference<>(this, entity);
        Archetype at = alloc.getArchetype(entity);
        int i = alloc.getArchetypeIndex(entity);
        ref.__0(at.getComponentById(componentId));
        ref.__1(i);

        IntObjectHashMap<ComponentReference<?>> refs = references.get(entity);
        if (refs == null) {
            refs = new IntObjectHashMap<>();
            references.put(entity, refs);
        }
        refs.put(componentId, ref);

        return ref;
    }

    public void updateLocationIndex(int entity, int newIndex) {
        IntObjectHashMap<ComponentReference<?>> refs = references.get(entity);
        if (refs != null) {
            for (var o : refs) {
                o.value.__1(newIndex);
            }
        }
    }

    public void destroyComponentReference(ComponentReference<?> compRef) {
        IntObjectHashMap<ComponentReference<?>> refs = references.get(compRef.getEntity());
        if (refs != null) {
            int key = -1;
            for (var o : refs) {
                if (o.value == compRef) {
                    key = o.key;
                }
            }

            if (key != -1) {
                refs.remove(key);
            }
        }
    }
}

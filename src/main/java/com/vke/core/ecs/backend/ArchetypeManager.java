package com.vke.core.ecs.backend;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.vke.core.ecs.ComponentReference;
import com.vke.core.ecs.api.EntityTransitionInitializer;
import com.vke.core.ecs.backend.query.QueryManager;
import com.vke.core.ecs.component.Component;
import com.vke.core.ecs.component.mask.ComponentMask;

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

    public int duplicateEntity(int entity) {
        Archetype at = alloc.getArchetype(entity);
        int index = alloc.getArchetypeIndex(entity);
        return at.duplicateEntity(index, alloc, qm);
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

    public void destroyEntities(int[] entities, int start, int length) {
        if (length < BATCHED_PATH_THRESHOLD) {
            for (int i = start; i < length; i++) {
                int entity = entities[i];
                destroyEntity(entity);
                cleanupRefsForEntity(entity);
            }
        } else {
            int maxArch = Archetype.IDS;
            int educatedGuess = Math.min(length / 50, Math.min(50, maxArch));
            IntObjectHashMap<IntArrayList> byArch = new IntObjectHashMap<>(educatedGuess);
            for (int i = start; i < length; i++) {
                int entity = entities[i];
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

    public ComponentReference<? extends Component> obtainCompRef(int entity, int componentId) {
        IntObjectHashMap<ComponentReference<?>> refs = references.get(entity);
        ComponentReference<?> ref;
        if (refs != null && (ref = refs.get(componentId)) != null) return ref;

        ref = new ComponentReference<>(this, entity);
        Archetype at = alloc.getArchetype(entity);
        int i = alloc.getArchetypeIndex(entity);
        ref.__0(at.getComponentById(componentId));
        ref.__1(i);

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

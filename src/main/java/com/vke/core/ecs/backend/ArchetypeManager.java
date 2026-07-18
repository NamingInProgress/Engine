package com.vke.core.ecs.backend;

import com.vke.core.ecs.api.EntityTransitionInitializer;
import com.vke.core.ecs.component.Component;
import com.vke.core.ecs.component.mask.ComponentMask;

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
}

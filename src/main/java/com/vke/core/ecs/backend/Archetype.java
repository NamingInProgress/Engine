package com.vke.core.ecs.backend;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.vke.api.annotation.PotentiallyUnsafe;
import com.vke.api.rendering.vulkan.buffer.CpuBuffer;
import com.vke.core.ecs.api.EntityInitializer;
import com.vke.core.ecs.component.Component;
import com.vke.core.ecs.component.mask.ComponentMask;

import java.util.Arrays;

public class Archetype {
    private final static int MAX_COMPONENTS_FOR_SPARSE_ARRAY = 4096;

    private final ComponentMask mask;
    private int[] owners;
    private int entryAmount;

    private final Component[] compArr;
    private final int[] idxToId;
    //JUST TEMPORARY REPLACE WITH MORE EFFICIENT IMMUTABLE MAP
    private final IntObjectHashMap<Component> compMap;

    public static int IDS = 0;
    private final int id = IDS++;

    public Archetype(ComponentMask mask, int usedComponents, ComponentRegistry registry) {
        this.mask = mask;
        this.entryAmount = 0;
        this.owners = new int[0];

        if (usedComponents > MAX_COMPONENTS_FOR_SPARSE_ARRAY) {
            this.compArr = new Component[mask.componentCount()];
            this.compMap = new IntObjectHashMap<>(usedComponents);
            this.idxToId = new int[usedComponents];
        } else {
            this.compArr = new Component[usedComponents];
            this.compMap = null;
            this.idxToId = null;
        }

        createComponents(mask, registry);
    }

    public ComponentMask getMask() {
        return mask;
    }

    public int getEntryAmount() {
        return entryAmount;
    }

    public int[] spawnEntities(int amount, EntityInitializer initializer, EntityAllocator allocator) {
        int left = entryAmount;
        entryAmount += amount;
        for (Component component : compArr) {
            component.resize(entryAmount);
        }

        allocator.allocateEntities(amount);

        owners = Arrays.copyOf(owners, entryAmount);

        int[] entities = new int[amount];
        for (int i = 0; i < amount; i++) {
            int id = allocator.genEntityId(this, left + i);
            initializer.initialize(this, left, entryAmount - 1, i);
            entities[i] = id;
            owners[i + left] = id;
        }
        return entities;
    }

    public void destroyEntity(int index, EntityAllocator allocator) {
        int movedEntity = removeAt(index, allocator);
        allocator.freeEntityId(movedEntity);
    }

    public void dangleEntity(int index, EntityAllocator allocator) {
        removeAt(index, allocator);
    }

    private int removeAt(int index, EntityAllocator allocator) {
        int lastIndex = --entryAmount;
        int movedEntity = owners[lastIndex];

        if (index != lastIndex) {
            for (Component component : compArr) {
                component.swap(index, lastIndex);
            }
            allocator.updateLocationIndex(movedEntity, index);
            owners[index] = movedEntity;
        }

        return movedEntity;
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponentById(int id) {
        if (compMap == null) return (T) compArr[id];
        return (T) compMap.get(id);
    }


    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponentByLocalIndex(int index) {
        if (idxToId != null) return (T) compArr[idxToId[index]];
        return (T) compArr[index];
    }

    public Component[] getComponents() {
        return compArr;
    }

    private void createComponents(ComponentMask mask, ComponentRegistry registry) {
        int[] ids = mask.getComponents();
        if (compMap == null) {
            for (int id : ids) {
                compArr[id] = registry.getInstance(id);
            }
        } else {
            for (int i = 0, idsLength = ids.length; i < idsLength; i++) {
                int id = ids[i];
                Component instance = registry.getInstance(id);
                compMap.put(id, instance);
                compArr[i] = instance;
                idxToId[i] = id;
            }
        }
    }

    public int accomodateDangling(int entity) {
        int newIdx = entryAmount++;
        while (owners.length < entryAmount) {
            int newSize = (int) (((double) owners.length) * CpuBuffer.GROWTH_FAC);
            owners = Arrays.copyOf(owners, newSize);
        }
        for (Component component : compArr) {
            component.resize(entryAmount);
        }
        owners[newIdx] = entity;
        return newIdx;
    }

    public int getId() {
        return id;
    }
}
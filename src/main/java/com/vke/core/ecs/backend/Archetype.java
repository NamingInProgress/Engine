package com.vke.core.ecs.backend;

import com.carrotsearch.hppc.IntObjectHashMap;
import com.carrotsearch.hppc.ObjectArrayList;
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

    private final int id;

    public static int IDS = 0;
    private static final ObjectArrayList<Archetype> ALL = new ObjectArrayList<>();

    public Archetype(ComponentMask mask, int usedComponents) {
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

        createComponents(mask);

        this.id = IDS++;
        ALL.set(id, this);
    }

    public static Archetype findById(int id) {
        return ALL.get(id);
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
            if (initializer != null) {
                initializer.initialize(this, left, entryAmount - 1, i);
            }
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

    private void createComponents(ComponentMask mask) {
        int[] ids = mask.getComponents();
        if (compMap == null) {
            for (int id : ids) {
                compArr[id] = ComponentRegistry.getInstance(id);
            }
        } else {
            for (int i = 0, idsLength = ids.length; i < idsLength; i++) {
                int id = ids[i];
                Component instance = ComponentRegistry.getInstance(id);
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

    public void destroyConsecutiveEntities(int index, int length, EntityAllocator alloc) {
        //fast path: we are at the end. unlikely, but much much much much faster
        int right = index + length;
        if (right == entryAmount) {
            entryAmount -= length;
            return;
        }

        int tailAmount = entryAmount - right;
        int toCopy = Math.min(tailAmount, length);
        int copyStart = entryAmount - toCopy;
        for (Component component : compArr) {
            component.copyRange(copyStart, index, toCopy);
        }

        for (int i = 0; i < toCopy; i++) {
            int oldIdx = i + copyStart;
            int newIdx = i + index;
            int entity = owners[oldIdx];
            int oldEntity = owners[newIdx];
            owners[newIdx] = entity;
            alloc.freeEntityId(oldEntity);
            alloc.updateLocationIndex(entity, newIdx);
        }

        entryAmount -= length;
    }
}
package com.vke.core.ecs.backend;

import com.carrotsearch.hppc.ObjectArrayList;
import com.vke.core.ecs.api.EntityInitializer;
import com.vke.core.ecs.backend.query.QueryManager;
import com.vke.core.ecs.component.Component;
import com.vke.core.ecs.component.mask.ComponentMask;

import java.util.Arrays;

public class Archetype {
    private final ComponentMask mask;
    private int[] owners;
    private int entryAmount;

    private final Component[] compArr;

    private final int[] idToLocal;

    private final int id;

    public static int IDS = 0;
    private static final ObjectArrayList<Archetype> ALL = new ObjectArrayList<>();

    public Archetype(ComponentMask mask, int totalComponentTypesCount) {
        this.mask = mask;
        this.entryAmount = 0;
        this.owners = new int[0];

        int numComponentsInArch = mask.componentCount();
        this.compArr = new Component[numComponentsInArch];

        this.idToLocal = new int[totalComponentTypesCount];
        Arrays.fill(this.idToLocal, -1);

        createComponents(mask);

        this.id = IDS++;
        ALL.add(this);
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

    private void createComponents(ComponentMask mask) {
        int[] componentIds = mask.getComponents();
        for (int i = 0; i < componentIds.length; i++) {
            int globalId = componentIds[i];
            Component instance = ComponentRegistry.getInstance(globalId);

            compArr[i] = instance;
            idToLocal[globalId] = i;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponentById(int globalId) {
        int localIdx = idToLocal[globalId];
        if (localIdx == -1) return null;
        return (T) compArr[localIdx];
    }

    public Component[] getComponents() {
        return compArr;
    }

    public int[] spawnEntities(int amount, EntityInitializer initializer, EntityAllocator allocator, QueryManager qm) {
        int left = entryAmount;
        entryAmount += amount;

        for (Component component : compArr) {
            component.resize(entryAmount);
        }

        allocator.allocateEntities(amount);

        if (owners.length < entryAmount) {
            owners = Arrays.copyOf(owners, entryAmount);
        }

        int[] entities = new int[amount];
        for (int i = 0; i < amount; i++) {
            int entityId = allocator.genEntityId(this, left + i);
            if (initializer != null) {
                initializer.initialize(this, left, entryAmount - 1, i);
            }
            entities[i] = entityId;
            owners[left + i] = entityId;
        }

        qm.onEntityBatchSpawn(mask, this, left, amount);

        return entities;
    }

    public void destroyEntity(int index, EntityAllocator allocator, QueryManager qm) {
        qm.onEntityDestroyed(this);
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

    public int accomodateDangling(int entity) {
        int newIdx = entryAmount++;
        if (owners.length < entryAmount) {
            int newSize = Math.max(4, (int) (owners.length * 1.5f));
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

    public void destroyConsecutiveEntities(int index, int length, EntityAllocator alloc, QueryManager qm) {
        qm.onConsecutiveEntitiesDestroyed(this, length);

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
            int oldIdx = copyStart + i;
            int newIdx = index + i;
            int entity = owners[oldIdx];
            int oldEntity = owners[newIdx];

            owners[newIdx] = entity;
            alloc.freeEntityId(oldEntity);
            alloc.updateLocationIndex(entity, newIdx);
        }

        entryAmount -= length;
    }
}
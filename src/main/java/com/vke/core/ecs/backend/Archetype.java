package com.vke.core.ecs.backend;

import com.carrotsearch.hppc.IntArrayList;
import com.vke.core.ecs.api.EntityInitializer;
import com.vke.core.ecs.component.Component;
import com.vke.core.ecs.component.mask.ComponentMask;

import java.util.Arrays;

public class Archetype {
    private final ComponentMask mask;
    //sorted!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! by id
    private final Component[] components;
    private int[] owners;
    private int entryAmount;

    public Archetype(ComponentMask mask) {
        this.mask = mask;
        this.components = createComponents(mask);
        this.entryAmount = 0;
        this.owners = new int[0];
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
        for (Component component : components) {
            component.makeRoom(entryAmount);
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
        int lastIndex = --entryAmount;
        int lastEntityId = owners[lastIndex];
        if (index != lastIndex) {
            for (Component component : components) {
                component.swap(index, entryAmount);
            }
            allocator.updateLocationIndex(lastEntityId, index);
            owners[index] = lastEntityId;
        }
        allocator.freeEntityId(owners[index]);
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponentById(int id) {
        int left = 0;
        int right = components.length - 1;
        int mid;
        while (left <= right) {
            mid = (left + right) >>> 1;
            int midId = components[mid].getId();

            if (midId < id) {
                left = mid + 1;
            } else if (midId > id) {
                right = mid - 1;
            } else {
                return (T) components[mid];
            }
        }
        return null;
    }

    private static Component[] createComponents(ComponentMask mask) {
        return null;
    }
}
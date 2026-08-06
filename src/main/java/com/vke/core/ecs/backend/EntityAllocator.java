package com.vke.core.ecs.backend;

import com.carrotsearch.hppc.IntArrayDeque;

import java.util.Arrays;

public class EntityAllocator {
    //im using a page system here to avoid huge reallocs. the numbers here are probably not well chosen
    private final static int LOCATION_TABLE_PAGE_SIZE = 256;
    private final static int LOCATION_TABLE_PAGE_MASK = LOCATION_TABLE_PAGE_SIZE - 1;
    private final static int LOCATION_TABLE_SHIFT = Integer.numberOfTrailingZeros(LOCATION_TABLE_PAGE_SIZE); //fast path for ilog2 for numbers that are power of 2
    private final static int LOCATION_TABLE_BASE_PAGES = 1;

    private int[][] locationTableIndices;
    private Archetype[][] locationTableArchetypes;
    private int capacity;
    private int nextId;
    private int pageSpaceLeft;
    private int pageIndex;
    private final IntArrayDeque freeQueue;

    public EntityAllocator() {
        this.locationTableIndices = new int[LOCATION_TABLE_BASE_PAGES][LOCATION_TABLE_PAGE_SIZE];
        this.locationTableArchetypes = new Archetype[LOCATION_TABLE_BASE_PAGES][LOCATION_TABLE_PAGE_SIZE];
        this.nextId = 0;
        this.pageSpaceLeft = LOCATION_TABLE_PAGE_SIZE;
        this.freeQueue = new IntArrayDeque();
    }

    public int getArchetypeIndex(int entity) {
        return locationTableIndices[entity >>> LOCATION_TABLE_SHIFT][entity & LOCATION_TABLE_PAGE_MASK];
    }

    public void setArchetypeIndex(int entity, int newIndex) {
        locationTableIndices[entity >>> LOCATION_TABLE_SHIFT][entity & LOCATION_TABLE_PAGE_MASK] = newIndex;
    }

    public Archetype getArchetype(int entity) {
        if (entity == 255) {
            System.out.println();
        }
        return locationTableArchetypes[entity >>> LOCATION_TABLE_SHIFT][entity & LOCATION_TABLE_PAGE_MASK];
    }

    public void setArchetype(int entity, Archetype archetype) {
        locationTableArchetypes[entity >>> LOCATION_TABLE_SHIFT][entity & LOCATION_TABLE_PAGE_MASK] = archetype;
    }

    public void allocateEntities(int amount) {
        int needed = Math.max(0, amount - freeQueue.size());
        if (needed != 0) {
            reallocLocationTable(capacity + needed);
        }
    }

    public int genEntityId(Archetype archetype, int archetypeIndex) {
        if (!freeQueue.isEmpty()) {
            int id = freeQueue.removeLast();
            setArchetypeIndex(id, archetypeIndex);
            setArchetype(id, archetype);
            return id;
        }

        int id = nextId++;
        pageSpaceLeft--;
        if (pageSpaceLeft < 0) {
            pageIndex++;
            pageSpaceLeft = LOCATION_TABLE_PAGE_SIZE - 1;
        }

        int onPageIndex = LOCATION_TABLE_PAGE_SIZE - (pageSpaceLeft + 1);
        int[] indexPage = locationTableIndices[pageIndex];
        Archetype[] archPage = locationTableArchetypes[pageIndex];
        indexPage[onPageIndex] = archetypeIndex;
        archPage[onPageIndex] = archetype;
        return id;
    }

    public void updateLocationIndex(int entity, int newIndex) {
        //after swap remove i have to recompute the swapped entities location
        setArchetypeIndex(entity, newIndex);
    }

    public void freeEntityId(int entity) {
        freeQueue.addFirst(entity);
    }

    private void reallocLocationTable(int newSize) {
        int oldPages = locationTableIndices.length;

        //fast path for ceilDiv(n, x) where x is a power of two i love these hacks bro
        int newPages = (newSize + LOCATION_TABLE_PAGE_SIZE - 1) >> LOCATION_TABLE_SHIFT;
        locationTableIndices = Arrays.copyOf(locationTableIndices, newPages);
        locationTableArchetypes = Arrays.copyOf(locationTableArchetypes, newPages);

        for (int i = oldPages; i < newPages; i++) {
            locationTableIndices[i] = new int[LOCATION_TABLE_PAGE_SIZE];
            locationTableArchetypes[i] = new Archetype[LOCATION_TABLE_PAGE_SIZE];
        }

        capacity = newSize;
    }
}

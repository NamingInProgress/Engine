package com.vke.core.game.scene;

import com.carrotsearch.hppc.IntArrayList;

import java.util.Arrays;

import java.util.Arrays;

import java.util.Arrays;

public class SceneGraph {
    private int[] parent;
    private int[] firstChild;
    private int[] nextSibling;
    private int[] prevSibling;

    private int capacity;

    public SceneGraph(int initialCapacity) {
        this.capacity = initialCapacity;
        int arraySize = initialCapacity + 1;

        this.parent = new int[arraySize];
        this.firstChild = new int[arraySize];
        this.nextSibling = new int[arraySize];
        this.prevSibling = new int[arraySize];

        clearAndReset();
    }

    private void clearAndReset() {
        Arrays.fill(parent, 0);
        Arrays.fill(firstChild, -1);
        Arrays.fill(nextSibling, -1);
        Arrays.fill(prevSibling, -1);
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > capacity) {
            int newCap = Math.max(capacity * 2, minCapacity);
            int arraySize = newCap + 1;

            int oldSize = parent.length;
            parent = Arrays.copyOf(parent, arraySize);
            firstChild = Arrays.copyOf(firstChild, arraySize);
            nextSibling = Arrays.copyOf(nextSibling, arraySize);
            prevSibling = Arrays.copyOf(prevSibling, arraySize);

            Arrays.fill(parent, oldSize, arraySize, 0);
            Arrays.fill(firstChild, oldSize, arraySize, -1);
            Arrays.fill(nextSibling, oldSize, arraySize, -1);
            Arrays.fill(prevSibling, oldSize, arraySize, -1);

            this.capacity = newCap;
        }
    }

    public void attachToParent(int entity, int parentEntity) {
        ensureCapacity(Math.max(entity, parentEntity) + 1);

        detachFromParentInternal(entity);

        int internalIdx = entity + 1;
        int internalParent = parentEntity + 1;

        this.parent[internalIdx] = internalParent;

        int currentFirst = firstChild[internalParent];
        nextSibling[internalIdx] = currentFirst;
        prevSibling[internalIdx] = -1;

        if (currentFirst != -1) {
            prevSibling[currentFirst + 1] = entity;
        }
        firstChild[internalParent] = entity;
    }

    public void detachParent(int entity) {
        attachToParent(entity, -1);
    }

    private void detachFromParentInternal(int entity) {
        int internalIdx = entity + 1;
        int internalParent = parent[internalIdx];

        int prev = prevSibling[internalIdx];
        int next = nextSibling[internalIdx];

        if (prev != -1) {
            nextSibling[prev + 1] = next;
        } else if (firstChild[internalParent] == entity) {
            firstChild[internalParent] = next;
        }

        if (next != -1) {
            prevSibling[next + 1] = prev;
        }

        parent[internalIdx] = 0;
        nextSibling[internalIdx] = -1;
        prevSibling[internalIdx] = -1;
    }

    public int iterChildren(int entity) {
        return firstChild[entity + 1];
    }

    public int nextChild(int last) {
        return last == -1 ? -1 : nextSibling[last + 1];
    }

    public int prevChild(int last) {
        return last == -1 ? -1 : prevSibling[last + 1];
    }

    public int parentOf(int entity) {
        return parent[entity + 1] - 1;
    }

    public void deleteNode(int entityId, IntArrayList deletedEntities) {
        detachFromParentInternal(entityId);
        deleteNodeInternal(entityId, deletedEntities);
    }

    private void deleteNodeInternal(int entityId, IntArrayList deletedEntities) {
        deletedEntities.add(entityId);

        int child = iterChildren(entityId);
        while (child != -1) {
            int next = nextChild(child);
            deleteNodeInternal(child, deletedEntities);
            child = next;
        }

        int internalIdx = entityId + 1;
        parent[internalIdx] = 0;
        firstChild[internalIdx] = -1;
        nextSibling[internalIdx] = -1;
        prevSibling[internalIdx] = -1;
    }
}
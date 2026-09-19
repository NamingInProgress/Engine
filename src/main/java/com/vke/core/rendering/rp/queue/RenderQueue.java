package com.vke.core.rendering.rp.queue;

import com.vke.core.rendering.rp.MeshInstance;

import java.util.Arrays;

public class RenderQueue {

    public MeshInstance[][] buckets;
    public int[] bucketSizes;

    public int[] activeKeys;
    public int activeKeyCount;

    private static final int INITIAL_BUCKET_COUNT = 64;
    private static final int INITIAL_BUCKET_CAPACITY = 32;

    public RenderQueue() {
        this.buckets = new MeshInstance[INITIAL_BUCKET_COUNT][];
        this.bucketSizes = new int[INITIAL_BUCKET_COUNT];
        this.activeKeys = new int[INITIAL_BUCKET_COUNT];
    }

    public void clear() {
        activeKeyCount = 0;
        Arrays.fill(bucketSizes, 0);
    }

    public void add(MeshInstance instance) {
        int key = instance.mesh().key();

        if (key >= buckets.length) {
            ensureOuterCapacity(key + 1);
        }

        if (buckets[key] == null) {
            buckets[key] = new MeshInstance[INITIAL_BUCKET_CAPACITY];
        }

        int size = bucketSizes[key];

        if (size == 0) {
            if (activeKeyCount == activeKeys.length) {
                activeKeys = Arrays.copyOf(activeKeys, activeKeys.length * 2);
            }
            activeKeys[activeKeyCount++] = key;
        }

        if (size == buckets[key].length) {
            buckets[key] = Arrays.copyOf(buckets[key], size * 2);
        }

        buckets[key][size] = instance;
        bucketSizes[key] = size + 1;
    }

    private void ensureOuterCapacity(int requiredCapacity) {
        int newCapacity = Math.max(buckets.length * 2, requiredCapacity);
        buckets = Arrays.copyOf(buckets, newCapacity);

        int[] newBucketSizes = new int[newCapacity];
        System.arraycopy(bucketSizes, 0, newBucketSizes, 0, bucketSizes.length);
        bucketSizes = newBucketSizes;
    }

}

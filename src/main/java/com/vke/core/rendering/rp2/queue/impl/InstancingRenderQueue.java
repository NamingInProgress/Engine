package com.vke.core.rendering.rp2.queue.impl;

import com.vke.core.rendering.rp.RenderKeyable;
import com.vke.core.rendering.rp2.queue.RenderQueue;

import java.lang.reflect.Array;
import java.util.Arrays;

public class InstancingRenderQueue<T extends RenderKeyable> implements RenderQueue<T> {

    public RenderKeyable[][] buckets;
    public int[] bucketSizes;

    public int[] activeKeys;
    public int activeKeyCount;

    private static final int INITIAL_BUCKET_COUNT = 64;
    private static final int INITIAL_BUCKET_CAPACITY = 32;

    public InstancingRenderQueue() {
        this.buckets = new RenderKeyable[INITIAL_BUCKET_COUNT][];
        this.bucketSizes = new int[INITIAL_BUCKET_COUNT];
        this.activeKeys = new int[INITIAL_BUCKET_COUNT];

        this.buckets[0] = new RenderKeyable[INITIAL_BUCKET_CAPACITY];
    }

    @Override
    public void clear() {
        activeKeyCount = 0;
        Arrays.fill(bucketSizes, 0);
    }

    @Override
    public void add(T instance) {
        int key = instance.key();

        if (key >= buckets.length) {
            ensureOuterCapacity(key + 1);
        }

        if (buckets[key] == null) {
            buckets[key] = Arrays.copyOf(buckets[0], INITIAL_BUCKET_CAPACITY);
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

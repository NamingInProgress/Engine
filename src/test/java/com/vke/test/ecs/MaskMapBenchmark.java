package com.vke.test.ecs;

import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.backend.MaskMap;

import java.util.HashMap;
import java.util.Map;

public class MaskMapBenchmark {

    private static final int COUNT = 100_000_00;
    private static final int ITERATIONS = 5;

    public static void main(String[] args) {
        ComponentMask[] masks = new ComponentMask[COUNT];
        Archetype[] archetypes = new Archetype[COUNT];

        for (int i = 0; i < COUNT; i++) {
            masks[i] = new U64ComponentMask(i);
            archetypes[i] = new Archetype(null);
        }

        MaskMap maskMap = new MaskMap();
        Map<ComponentMask, Archetype> hashMap = new HashMap<>();

        for (int i = 0; i < COUNT; i++) {
            maskMap.insert(masks[i], archetypes[i]);
            hashMap.put(masks[i], archetypes[i]);
        }

        // warmup
        runMaskMap(maskMap, masks);
        runHashMap(hashMap, masks);

        long start = System.nanoTime();
        long a = 0;

        for (int i = 0; i < ITERATIONS; i++) {
            a += runMaskMap(maskMap, masks);
        }

        long maskTime = System.nanoTime() - start;


        start = System.nanoTime();
        long b = 0;

        for (int i = 0; i < ITERATIONS; i++) {
            b += runHashMap(hashMap, masks);
        }

        long hashTime = System.nanoTime() - start;


        System.out.println("MaskMap: " + maskTime / 1_000_000 + "ms");
        System.out.println("HashMap: " + hashTime / 1_000_000 + "ms");
        System.out.println("Prevent optimize: " + (a + b));
    }

    private static long runMaskMap(MaskMap map, ComponentMask[] masks) {
        long result = 0;

        for (ComponentMask mask : masks) {
            result += System.identityHashCode(map.find(mask));
        }

        return result;
    }

    private static long runHashMap(Map<ComponentMask, Archetype> map, ComponentMask[] masks) {
        long result = 0;

        for (ComponentMask mask : masks) {
            result += System.identityHashCode(map.get(mask));
        }

        return result;
    }
}
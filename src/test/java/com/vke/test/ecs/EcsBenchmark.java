package com.vke.test.ecs;

import com.vke.core.ecs.services.EcsManagerImpl;
import com.vke.core.ecs.EcsCreateInfo;
import com.vke.core.ecs.api.Query;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.component.Component;
import com.vke.core.ecs.component.mask.ComponentMask;
import pl.epsi.EcsComponent;

public class EcsBenchmark {

    private static final int ENTITY_COUNT = 100_000;
    private static final int WARMUP_RUNS = 5;
    private static final int BENCHMARK_RUNS = 10;

    public static void main(String[] args) {
        int _h = Health.ID;
        int _p = Position.ID;

        System.out.println("==================================================");
        System.out.println("           ECS ARCHETYPE ENGINE BENCHMARK          ");
        System.out.println("==================================================");
        System.out.printf("Entities per test: %,d%n", ENTITY_COUNT);

        // Warmup phase (allows JIT compiler to inline methods and optimize hot loops)
        System.out.println("\n[1/2] Running JIT Warmup...");
        for (int i = 0; i < WARMUP_RUNS; i++) {
            runBenchmarkSuite(true);
        }

        // Measured Benchmark phase
        System.out.println("\n[2/2] Running Benchmarks...");
        runBenchmarkSuite(false);
    }

    private static void runBenchmarkSuite(boolean isWarmup) {
        EcsCreateInfo createInfo = new EcsCreateInfo();
        EcsManagerImpl ecs = new EcsManagerImpl(createInfo);

        int systemCategory = ecs.createCategory();
        ecs.registerQuery(systemCategory, new BenchmarkHealthQuery());

        ComponentMask posHealthMask = new ComponentMask(Position.ID, Health.ID);
        ComponentMask healthOnlyMask = new ComponentMask(Health.ID);

        // -----------------------------------------------------------------
        // TEST 1: Batch Entity Spawning
        // -----------------------------------------------------------------
        long start = System.nanoTime();

        int[] entities = ecs.spawnEntities(ENTITY_COUNT, posHealthMask, (at, base, maxIndex, idx) -> {
            Health h = at.getComponentById(Health.ID);
            Position p = at.getComponentById(Position.ID);

            int targetIndex = base + idx;
            h.hp[targetIndex] = idx * 10;
            p.x[targetIndex] = idx * 1.5f;
            p.y[targetIndex] = idx * 2.0f;
            p.z[targetIndex] = idx * 0.5f;
        });

        long spawnTime = System.nanoTime() - start;

        // -----------------------------------------------------------------
        // TEST 2: Query Execution / Iteration Speed
        // -----------------------------------------------------------------
        BenchmarkHealthQuery.resetSum();
        start = System.nanoTime();

        ecs.runQueries(systemCategory);

        long queryTime = System.nanoTime() - start;

        // -----------------------------------------------------------------
        // TEST 3: Archetype Transitions (Move Position+Health -> Health)
        // -----------------------------------------------------------------
        start = System.nanoTime();

        // Transition half of the entities to a different archetype
        int half = ENTITY_COUNT / 2;
        for (int i = 0; i < half; i++) {
            ecs.transitionEntity(entities[i], healthOnlyMask, null);
        }

        long transitionTime = System.nanoTime() - start;

        // -----------------------------------------------------------------
        // TEST 4: Query Execution with Split Archetypes
        // -----------------------------------------------------------------
        start = System.nanoTime();

        ecs.runQueries(systemCategory);

        long splitQueryTime = System.nanoTime() - start;

        // -----------------------------------------------------------------
        // TEST 5: Bulk Entity Destruction
        // -----------------------------------------------------------------
        start = System.nanoTime();

        ecs.destroyEntities(entities);

        long destroyTime = System.nanoTime() - start;

        // Output results (only during actual benchmark runs)
        if (!isWarmup) {
            printResult("Batch Entity Spawn", spawnTime, ENTITY_COUNT);
            printResult("Query Iteration (Single Arch)", queryTime, ENTITY_COUNT);
            printResult("Archetype Transition (50%)", transitionTime, half);
            printResult("Query Iteration (Multi Arch)", splitQueryTime, ENTITY_COUNT);
            printResult("Bulk Entity Destroy", destroyTime, ENTITY_COUNT);
            System.out.println("--------------------------------------------------");
        }
    }

    private static void printResult(String label, long totalNanos, int count) {
        double ms = totalNanos / 1_000_000.0;
        double nsPerOp = (double) totalNanos / count;
        double opsPerSec = (count / (totalNanos / 1_000_000_000.0)) / 1_000_000.0; // Million ops/sec

        System.out.printf("%-30s | %8.2f ms | %6.1f ns/op | %6.2f M ops/s%n",
                label, ms, nsPerOp, opsPerSec);
    }

    // =========================================================================
    // BENCHMARK COMPONENTS & QUERY
    // =========================================================================

    @EcsComponent
    public static class Position implements Component {
        public float[] x, y, z;
    }

    @EcsComponent
    public static class Health implements Component {
        public int[] hp;
    }

    public static class BenchmarkHealthQuery implements Query {
        // Blackhole accumulator to prevent JIT from optimizing away loop iterations
        public static long checksum = 0;

        public static void resetSum() {
            checksum = 0;
        }

        @Override
        public ComponentMask getMask() {
            return new ComponentMask(Health.ID);
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {
            Health h = at.getComponentById(Health.ID);
            int[] hpArr = h.hp;

            long localSum = 0;
            for (int i = i0; i < i1; i++) {
                localSum += hpArr[i];
            }
            checksum += localSum;
        }
    }
}
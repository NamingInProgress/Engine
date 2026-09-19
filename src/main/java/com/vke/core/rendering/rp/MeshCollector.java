package com.vke.core.rendering.rp;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.core.ecs.api.Query;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.core.ecs.services.EcsManager;
import com.vke.core.rendering.rp.queue.RenderQueue;
import com.vke.core.rendering.rp.queue.RenderQueueHandler;
import com.vke.core.services2.Services;
import com.vke.impl.ecs.WorldTransformC;
import com.vke.impl.ecs.mesh.StaticMeshC;

public class MeshCollector {

    private final int RUN_CATEGORY, COUNT_CATEGORY, UPDATE_CATEGORY;

    private final RenderSystem sys;
    private final EcsManager ecs;
    public final RenderQueueHandler queueHandler;

    private long previousCount = -1;

    private boolean updated = false;

    public MeshCollector(RenderSystem sys) {
        this.sys = sys;
        this.ecs = sys.service(Services.ECS);
        this.queueHandler = new RenderQueueHandler();

        this.queueHandler.collect(sys); // TODO: Replace with service or sth

        this.RUN_CATEGORY = ecs.createCategory();
        this.COUNT_CATEGORY = ecs.createCategory();
        this.UPDATE_CATEGORY = ecs.createCategory();

        ecs.registerQuery(COUNT_CATEGORY, new StaticMeshCounterQuery());
        ecs.registerQuery(RUN_CATEGORY, new StaticMeshQuery());
        ecs.registerQuery(UPDATE_CATEGORY, new UpdateStaticMeshQuery());
    }

    public void buildRenderQueues() {
        long currentCount = ecs.runQueries(COUNT_CATEGORY);
        if (currentCount == previousCount) {
            ecs.runQueries(UPDATE_CATEGORY);
            updated = false;
            return;
        }

        queueHandler.clear();
        previousCount = currentCount;
        ecs.runQueries(RUN_CATEGORY);
    }

    private static class StaticMeshCounterQuery implements Query {
        @Override
        public ComponentMask getMask() {
            return ComponentMask.of(StaticMeshC.ID);
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {}
    }

    private class StaticMeshQuery implements Query {

        @Override
        public ComponentMask getMask() {
            return ComponentMask.of(StaticMeshC.ID);
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {
            StaticMeshC smc = at.getComponentById(StaticMeshC.ID);
            WorldTransformC wtc = at.getComponentById(WorldTransformC.ID);

            for (int i = i0; i < i1; i++) {
                float[] mat = new float[16];
                wtc.getWorldMatrix(i, mat);
                queueHandler.enqueue(smc.renderQueueKey[i], new MeshInstance(i, smc.mesh[i], mat, smc.material[i]));
            }
        }
    }

    private class UpdateStaticMeshQuery implements Query {

        @Override
        public ComponentMask getMask() {
            return ComponentMask.of(StaticMeshC.ID);
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {
            if (updated) return;
            updated = true;
            WorldTransformC wtc = at.getComponentById(WorldTransformC.ID);

            for (RenderQueue queue : queueHandler.queues) {
                for (int i = 0; i < queue.activeKeyCount; i++) {
                    int key = queue.activeKeys[i];
                    int count = queue.bucketSizes[key];
                    MeshInstance[] bucket = queue.buckets[key];

                    for (int j = 0; j < count; j++) {
                        MeshInstance mi = bucket[j];
                        if (mi == null) continue;

                        float[] mat = new float[16];
                        wtc.getWorldMatrix(mi.entityId(), mat);
                        mi.setMat(mat);
                    }
                }
            }
        }
    }

}

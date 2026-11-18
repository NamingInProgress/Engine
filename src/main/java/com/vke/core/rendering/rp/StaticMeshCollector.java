package com.vke.core.rendering.rp;

import com.vke.core.Identifier;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.services.EcsManager;
import com.vke.core.rendering.rp2.queue.RenderQueueCollector;
import com.vke.core.rendering.rp2.queue.impl.InstancingRenderQueue;
import com.vke.impl.ecs.WorldTransformC;
import com.vke.impl.ecs.mesh.StaticMeshC;

import java.util.ArrayList;

public class StaticMeshCollector extends AbstractRenderableCollector {

    public StaticMeshCollector(EcsManager ecs, RenderQueueCollector rqh) {
        super(ecs, rqh, new CountingQuery(rqh), new UpdateQuery(rqh), new RunQuery(rqh), Identifier.of("vke:deferred"), Identifier.of("vke:forward+"));
    }

    private static class CountingQuery extends RenderableQuery {

        public CountingQuery(RenderQueueCollector queueHandler) {
            super(queueHandler, StaticMeshC.ID);
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {}
    }

    private static class RunQuery extends RenderableQuery {

        public RunQuery(RenderQueueCollector queue) {
            super(queue, StaticMeshC.ID);
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

    private static class UpdateQuery extends RenderableQuery {

        private final ArrayList<InstancingRenderQueue<MeshInstance>> queues = new ArrayList<>();

        public UpdateQuery(RenderQueueCollector queueHandler) {
            super(queueHandler, StaticMeshC.ID);
            queues.add(queueHandler.getQueue(Identifier.of("vke:deferred")));
            queues.add(queueHandler.getQueue(Identifier.of("vke:forward+")));
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {
            if (updated) return;
            updated = true;

            WorldTransformC wtc = at.getComponentById(WorldTransformC.ID);
            StaticMeshC smc = at.getComponentById(StaticMeshC.ID);

            for (InstancingRenderQueue<MeshInstance> queue : queues) {
                for (int i = 0; i < queue.activeKeyCount; i++) {
                    int key = queue.activeKeys[i];
                    int count = queue.bucketSizes[key];
                    // I think this'll be fine because it is in theory a MeshInstance[]
                    RenderKeyable[] bucket = queue.buckets[key];

                    for (int j = 0; j < count; j++) {
                        MeshInstance mi = (MeshInstance) bucket[j];
                        if (mi == null) continue;

                        int eId = mi.entityId();
                        float[] mat = new float[16];
                        wtc.getWorldMatrix(eId, mat);
                        mi.setMat(mat);

                        if (mi.materialIndex() != smc.material[eId])
                            mi.setMaterial(smc.material[eId]);
                        if (mi.mesh().key() != smc.mesh[eId].key())
                            mi.setMesh(smc.mesh[eId]);
                    }
                }
            }
        }
    }

}

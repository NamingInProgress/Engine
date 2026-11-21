package com.vke.core.rendering.rp;

import com.vke.core.Identifier;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.services.EcsManager;
import com.vke.core.rendering.rp2.queue.RenderQueueCollector;
import com.vke.core.rendering.rp2.queue.impl.BasicRenderQueue;
import com.vke.impl.ecs.WorldTransformC;
import com.vke.impl.ecs.text.BasicTextC;

public class BasicTextCollector extends AbstractRenderableCollector {

    public BasicTextCollector(EcsManager ecs, RenderQueueCollector rqh) {
        super(ecs, rqh, new CountingQuery(rqh), new UpdateQuery(rqh), new RunQuery(rqh), Identifier.of("vke:basic-text"));
    }

    private static class CountingQuery extends RenderableQuery {

        public CountingQuery(RenderQueueCollector queueHandler) {
            super(queueHandler, BasicTextC.ID);
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {}
    }

    private static class RunQuery extends RenderableQuery {

        private final BasicRenderQueue<BasicTextInstance> basicTextQueue;

        public RunQuery(RenderQueueCollector queue) {
            super(queue, BasicTextC.ID);
            this.basicTextQueue = queue.getQueue(Identifier.of("vke:basic-text"));
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {
            WorldTransformC wtc = at.getComponentById(WorldTransformC.ID);
            BasicTextC btc = at.getComponentById(BasicTextC.ID);

            for (int i = i0; i < i1; i++) {
                float[] mat = new float[16];
                wtc.getWorldMatrix(i, mat);
                basicTextQueue.add(new BasicTextInstance(i, btc.text[i], btc.color[i], btc.fontScale[i], mat, btc.font[i], btc.selector[i]));
            }
        }
    }

    private static class UpdateQuery extends RenderableQuery {

        private final BasicRenderQueue<BasicTextInstance> basicTextQueue;

        public UpdateQuery(RenderQueueCollector queueHandler) {
            super(queueHandler, BasicTextC.ID);
            this.basicTextQueue = queueHandler.getQueue(Identifier.of("vke:basic-text"));
        }

        @Override
        public void execute(Archetype at, int i0, int i1) {
            if (updated) return;
            updated = true;
            WorldTransformC wtc = at.getComponentById(WorldTransformC.ID);
            BasicTextC btc = at.getComponentById(BasicTextC.ID);

            for (BasicTextInstance instance : basicTextQueue.iterator()) {
                int eid = instance.entityId;
                float[] mat = new float[16];
                wtc.getWorldMatrix(eid, mat);
                instance.mat = mat;
                instance.text = btc.text[eid];
                instance.color = btc.color[eid];
                instance.fontScale = btc.fontScale[eid];
            }
        }
    }

}

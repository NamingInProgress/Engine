package com.vke.core.rendering.rp;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.core.ecs.services.EcsManager;
import com.vke.core.rendering.rp.queue.RenderQueueHandler;
import com.vke.core.rendering.rp2.queue.RenderQueue;
import com.vke.core.rendering.rp2.queue.RenderQueueCollector;
import com.vke.core.services2.Services;

import java.util.ArrayList;

public class RenderingCollector {

    private final RenderSystem sys;
    private final EcsManager ecs;
    public final RenderQueueCollector queueHandler;

    private final ArrayList<CollectionQueryInfo> collectors = new ArrayList<>();

    public RenderingCollector(RenderSystem sys) {
        this.sys = sys;
        this.ecs = sys.service(Services.ECS);
        this.queueHandler = new RenderQueueCollector(sys); // TODO: Replace with service

        registerCollector(new StaticMeshCollector(ecs, queueHandler));
        //registerCollector(new BasicTextCollector(ecs, queueHandler)); // TODO: Fix this
    }

    public void registerCollector(AbstractRenderableCollector col) {
        this.collectors.add(new CollectionQueryInfo(col));
    }

    public void buildRenderQueues() {
        for (CollectionQueryInfo info : collectors) {
            AbstractRenderableCollector collector = info.collector;
            long currentCount = ecs.runQueries(collector.countingQuery);
            if (currentCount == info.previousCount) {
                ecs.runQueries(collector.updateQuery);
                collector.setUpdated(false);
                continue;
            }

            for (RenderQueue<?> queue : collector.queues()) {
                queue.clear();
            }

            info.previousCount = currentCount;
            ecs.runQueries(collector.runQuery);
        }
    }

    public static class CollectionQueryInfo {
        private final AbstractRenderableCollector collector;

        public long previousCount;
        public boolean updated;

        public CollectionQueryInfo(AbstractRenderableCollector collector) {
            this.collector = collector;
            this.previousCount = 0;
            this.updated = false;
        }
    }

}

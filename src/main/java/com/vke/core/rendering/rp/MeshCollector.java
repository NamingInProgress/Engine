package com.vke.core.rendering.rp;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.core.ecs.api.Query;
import com.vke.core.ecs.backend.Archetype;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.core.ecs.services.EcsManager;
import com.vke.core.rendering.rp.queue.RenderQueue;
import com.vke.core.rendering.rp.queue.RenderQueueCollector;
import com.vke.core.services2.Services;
import com.vke.impl.ecs.mesh.StaticMeshC;

public class MeshCollector {

    private final int RUN_CATEGORY, COUNT_CATEGORY;

    private final RenderSystem sys;
    private final EcsManager ecs;
    private final RenderQueueCollector queues;

    private long previousCount = -1;

    public MeshCollector(RenderSystem sys) {
        this.sys = sys;
        this.ecs = sys.service(Services.ECS);
        this.queues = new RenderQueueCollector();

        this.queues.collect(sys); // TODO: Replace with service or sth

        this.RUN_CATEGORY = ecs.createCategory();
        this.COUNT_CATEGORY = ecs.createCategory();

        ecs.registerQuery(COUNT_CATEGORY, new StaticMeshCounterQuery());

        ecs.registerQuery(RUN_CATEGORY, new StaticMeshQuery());
    }

    public void buildRenderQueues() {
        long currentCount = ecs.runQueries(COUNT_CATEGORY);
        if (currentCount == previousCount) return;

        previousCount = currentCount;

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
            //queues.queues[]
        }
    }

}

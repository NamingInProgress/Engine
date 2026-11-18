package com.vke.core.rendering.rp;

import com.vke.core.ecs.api.Query;
import com.vke.core.ecs.component.mask.ComponentMask;
import com.vke.core.rendering.rp.queue.RenderQueueHandler;
import com.vke.core.rendering.rp2.queue.RenderQueueCollector;

public abstract class RenderableQuery implements Query {

    private final ComponentMask mask;

    protected final RenderQueueCollector queueHandler;

    public boolean updated;

    public RenderableQuery(RenderQueueCollector queueHandler, int... components) {
        this.mask = ComponentMask.of(components);
        this.queueHandler = queueHandler;
    }

    @Override
    public ComponentMask getMask() {
        return mask;
    }

}

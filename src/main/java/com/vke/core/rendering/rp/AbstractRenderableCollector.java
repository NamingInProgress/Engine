package com.vke.core.rendering.rp;

import com.vke.core.Identifier;
import com.vke.core.ecs.services.EcsManager;
import com.vke.core.rendering.rp2.queue.RenderQueue;
import com.vke.core.rendering.rp2.queue.RenderQueueCollector;

import java.util.List;

public abstract class AbstractRenderableCollector {

    protected final int countingQuery, updateQuery, runQuery;
    protected final RenderableQuery updateQueryInstance;

    protected final RenderQueue<?>[] queues;

    public AbstractRenderableCollector(EcsManager ecs, RenderQueueCollector rqh,
                                       RenderableQuery countingQuery, RenderableQuery updateQuery, RenderableQuery runQuery,
                                       Identifier... usedQueues) {
        this.countingQuery = ecs.createCategory();
        this.updateQuery = ecs.createCategory();
        this.runQuery = ecs.createCategory();

        this.updateQueryInstance = updateQuery;

        this.queues = new RenderQueue[usedQueues.length];
        for (int i = 0; i < usedQueues.length; i++) {
            Identifier usedQueue = usedQueues[i];
            queues[i] = rqh.getQueue(usedQueue);
        }

        ecs.registerQuery(this.countingQuery, countingQuery);
        ecs.registerQuery(this.updateQuery, updateQuery);
        ecs.registerQuery(this.runQuery, runQuery);
    }

    public int getCountingQuery() {
        return countingQuery;
    }

    public int getUpdateQuery() {
        return updateQuery;
    }

    public int getRunQuery() {
        return runQuery;
    }

    public void setUpdated(boolean val) {
        updateQueryInstance.updated = val;
    }

    public RenderQueue<?>[] queues() { return this.queues; }

}

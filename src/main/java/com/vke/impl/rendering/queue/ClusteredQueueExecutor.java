package com.vke.impl.rendering.queue;

import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.rendering.rp.queue.RenderQueue;
import com.vke.core.rendering.rp.queue.RenderQueueExecutor;

public class ClusteredQueueExecutor extends RenderQueueExecutor {
    @Override
    public void acceptQueue(RenderQueue queue, CommandBuffer cmd, RenderPass pass) {

    }
}

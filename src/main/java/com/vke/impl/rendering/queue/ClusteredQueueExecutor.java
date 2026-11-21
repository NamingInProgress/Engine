package com.vke.impl.rendering.queue;

import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.rendering.rp.MeshInstance;
import com.vke.core.rendering.rp2.queue.RenderQueue;
import com.vke.core.rendering.rp2.queue.RenderQueueExecutor;

public class ClusteredQueueExecutor extends RenderQueueExecutor<MeshInstance> {
    @Override
    public void acceptQueue(RenderQueue<MeshInstance> queue, CommandBuffer cmd, RenderPass pass) {

    }
}

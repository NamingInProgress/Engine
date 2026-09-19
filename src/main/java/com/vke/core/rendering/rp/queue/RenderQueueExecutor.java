package com.vke.core.rendering.rp.queue;

import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.rendering.graph.RenderPassInstance;

public abstract class RenderQueueExecutor {

    public abstract void acceptQueue(RenderQueue queue, CommandBuffer cmd, RenderPass pass, RenderPassInstance instance);

}

package com.vke.core.rendering.rp.queue;

import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.rendering.rp.RenderKeyable;

public abstract class RenderQueueExecutor<T extends RenderKeyable> {

    public void onLoad(RenderPass pass) {}

    public abstract void acceptQueue(SortedRenderQueue<T> queue, CommandBuffer cmd, RenderPass pass);

}

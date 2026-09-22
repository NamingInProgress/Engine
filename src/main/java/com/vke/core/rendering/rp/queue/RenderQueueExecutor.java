package com.vke.core.rendering.rp.queue;

import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.core.rendering.graph2.renderpass.RenderPass;

public abstract class RenderQueueExecutor {

    public void onLoad(RenderPass pass) {}

    public abstract void acceptQueue(RenderQueue queue, CommandBuffer cmd, RenderPass pass);

}

package com.vke.core.rendering.rp2.queue;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.core.rendering.graph2.renderpass.RenderPass;

public abstract class RenderQueueExecutor<T> {

    public void onLoad(RenderSystem sys, RenderPass pass) {}

    public abstract void acceptQueue(RenderQueue<T> queue, CommandBuffer cmd, RenderPass pass);

}

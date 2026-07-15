package com.vke.api.rendering.abstraction.rendergraph;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;

public abstract class RenderPass {

    protected final RenderSystem renderSystem;
    protected final RenderPassInstance instance;

    public RenderPass(RenderSystem renderSystem, RenderPassInstance instance) {
        this.renderSystem = renderSystem;
        this.instance = instance;
    }

    public abstract void execute(CommandBuffer cmd, RenderGraph graph);

}

package com.vke.api.rendering.abstraction.rendergraph;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderGraph;
import com.vke.core.rendering.graph.RenderPassInstance;

public abstract class RenderPass {

    protected final RenderSystem renderSystem;
    protected final RenderPassInstance instance;

    public RenderPass(RenderSystem renderSystem, RenderPassInstance instance) {
        this.renderSystem = renderSystem;
        this.instance = instance;
    }

    public void onLoad() {}

    public abstract void execute(CommandBuffer cmd, GraphContext context);

}

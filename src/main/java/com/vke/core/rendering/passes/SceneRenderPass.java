package com.vke.core.rendering.passes;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.core.rendering.rp.MeshCollector;

public class SceneRenderPass extends RenderPass {

    private final MeshCollector mc;

    public SceneRenderPass(RenderSystem renderSystem, RenderPassInstance instance) {
        super(renderSystem, instance);
        this.mc = new MeshCollector(renderSystem);
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        mc.buildRenderQueues();
        mc.queueHandler.render(cmd, this, instance);
    }

}

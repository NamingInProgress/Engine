package com.vke.core.rendering.graph2.renderpass;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.rendering.graph2.RenderGraph;
import com.vke.core.rendering.rp.MeshCollector;

public class SceneRenderPass extends RenderPass {

    private final MeshCollector mc;

    public SceneRenderPass(RenderSystem sys, RenderGraph graph, Def def) {
        super(sys, graph, def);
        this.mc = new MeshCollector(sys);
    }

    @Override
    public void onLoad() {
        this.mc.queueHandler.onLoad(this);
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        mc.buildRenderQueues();
        mc.queueHandler.render(cmd, this);
    }

}

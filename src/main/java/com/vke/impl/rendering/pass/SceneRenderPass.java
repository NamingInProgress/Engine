package com.vke.impl.rendering.pass;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.rendering.graph2.RenderGraph;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.rendering.rp.RenderingCollector;

public class SceneRenderPass extends RenderPass {

    private final RenderingCollector mc;

    public SceneRenderPass(RenderSystem sys, RenderGraph graph, Def def) {
        super(sys, graph, def);
        this.mc = new RenderingCollector(sys);
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

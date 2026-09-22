package com.vke.core.ui.rendering;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.rendering.graph2.RenderGraph;
import com.vke.core.rendering.graph2.renderpass.RenderPass;

public class UIRenderPass extends RenderPass {
    public UIRenderPass(RenderSystem renderSystem, RenderGraph graph, Def def) {
        super(renderSystem, graph, def);
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {

    }
}

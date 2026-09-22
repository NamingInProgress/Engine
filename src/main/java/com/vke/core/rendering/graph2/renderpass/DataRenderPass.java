package com.vke.core.rendering.graph2.renderpass;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.core.rendering.graph2.RenderGraph;

public abstract class DataRenderPass extends RenderPass {
    protected final ConfigArrayNode dataConfig;

    public DataRenderPass(RenderSystem sys, RenderGraph graph, Def def, ConfigArrayNode data) {
        super(sys, graph, def);
        this.dataConfig = data;
    }
}

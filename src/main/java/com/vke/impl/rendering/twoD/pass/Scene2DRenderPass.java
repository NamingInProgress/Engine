package com.vke.impl.rendering.twoD.pass;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.core.color.RgbColor;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.rendering.graph2.GraphTexture;
import com.vke.core.rendering.graph2.RenderGraph;
import com.vke.core.rendering.graph2.renderpass.RenderPass;

import java.util.List;

public class Scene2DRenderPass extends RenderPass {
    private GraphTexture color, depth;
    private List<GraphTexture> colorList;

    public Scene2DRenderPass(RenderSystem sys, RenderGraph graph, Def def) {
        super(sys, graph, def);
    }

    @Override
    public void onLoad() {
        this.color = searchOutputTexture("color");
        this.colorList = List.of(color);
        this.depth = searchOutputTexture("depth");
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        this.beginRendering(cmd, colorList, depth, RgbColor.WHITE, RgbColor.WHITE);
        //List<Scene2dDrawOps> ops = context.get("vke:scene2d:operations");
        cmd.endRendering();
    }
}

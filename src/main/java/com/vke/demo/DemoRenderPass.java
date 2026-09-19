package com.vke.demo;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.LoadOp;
import com.vke.api.rendering.abstraction.renderer.enums.StoreOp;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.rendering.graph2.GraphTexture;
import com.vke.core.rendering.graph2.RenderGraph;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.rendering.pipeline.RenderPipelines;

import java.util.List;

public class DemoRenderPass extends RenderPass {
    private GraphTexture colorOut;
    private GraphTexture depthOut;

    public DemoRenderPass(RenderSystem context, RenderGraph graph, Def def) {
        super(context, graph, def);
    }

    @Override
    public void onLoad() {
        colorOut = searchOutputTexture("colorOut");
        depthOut = searchOutputTexture("depthOut");
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        Texture color = colorOut.extract();
        Texture depth = depthOut.extract();
        cmd.beginRendering(new CommandBuffer.RenderingInfo(List.of(
                    new CommandBuffer.AttachmentInfo(color, LoadOp.CLEAR, StoreOp.STORE, new float[]{ 0.2f, 0.3f, 0.3f, 1.0f })
                ),
                new CommandBuffer.AttachmentInfo(depth, LoadOp.CLEAR, StoreOp.STORE, new float[]{ 1.0f })));

        RenderPipelines.DEFERRED.setLocal(context.get("localMat"));
        RenderPipelines.DEFERRED.use();
        DemoScene.MESH.draw();

        cmd.endRendering();
    }

}

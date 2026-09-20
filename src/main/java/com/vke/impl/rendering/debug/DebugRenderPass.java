package com.vke.impl.rendering.debug;

import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.core.color.RgbColor;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.rendering.graph2.GraphTexture;
import com.vke.core.rendering.graph2.RenderGraph;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.rendering.vulkan.command.VulkanCmdBuffers;
import com.vke.impl.rendering.driver.BasicPipelineDriver;
import com.vke.impl.rendering.vertex.DebugVertex;
import org.lwjgl.vulkan.VK14;

import java.util.List;

public class DebugRenderPass extends RenderPass {

    private VertexConsumer<DebugVertex> vc;
    private BasicPipelineDriver triangleDriver, linesDriver;

    private GraphTexture colorOut;
    private GraphTexture depthOut;

    public DebugRenderPass(RenderSystem renderSystem, RenderGraph graph, Def def) {
        super(renderSystem, graph, def);
    }

    @Override
    public void onLoad() {
        this.vc = sys.vcp().get(DebugVertex.TEMPLATE);
        this.triangleDriver = new BasicPipelineDriver(sys, R.pipelines.get("debug_3d_tri.pipeline.json"));
        this.linesDriver = new BasicPipelineDriver(sys, R.pipelines.get("debug_3d_lines.pipeline.json"));

        colorOut = searchOutputTexture("colorOut");
        depthOut = searchOutputTexture("depthOut");
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        this.beginRendering(cmd, List.of(colorOut), depthOut, RgbColor.VKE, RgbColor.WHITE);

        triangleDriver.use();
        DebugContext.tri_commands.forEach(c -> c.draw(vc));
        vc.draw();

        linesDriver.use();
        DebugContext.line_commands.forEach(c -> c.draw(vc));
        vc.draw();

        DebugContext.clear();

        cmd.endRendering();
    }

}

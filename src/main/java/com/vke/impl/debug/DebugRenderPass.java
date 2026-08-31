package com.vke.impl.debug;

import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.color.OldColor;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.core.rendering.vulkan.command.VulkanCmdBuffers;
import com.vke.impl.driver.BasicPipelineDriver;
import com.vke.impl.vertex.DebugVertex;
import org.lwjgl.vulkan.VK14;

import java.util.List;

public class DebugRenderPass extends RenderPass {

    private VertexConsumer<DebugVertex> vc;
    private BasicPipelineDriver driver;

    public DebugRenderPass(RenderSystem renderSystem, RenderPassInstance instance) {
        super(renderSystem, instance);
    }

    @Override
    public void onLoad() {
        this.vc = renderSystem.vcp().get(DebugVertex.TEMPLATE);
        this.driver = new BasicPipelineDriver(renderSystem, R.pipelines.get("debug_3d.pipeline.json"));
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        this.beginRendering(cmd, List.of("colorOut"), "depthOut", OldColor.VKE, OldColor.WHITE);

        driver.use();
        VK14.vkCmdSetPrimitiveTopology(((VulkanCmdBuffers) cmd).getBuffer(), VK14.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
        DebugContext.tri_commands.forEach(c -> c.draw(vc));
        vc.draw();

        VK14.vkCmdSetPrimitiveTopology(((VulkanCmdBuffers) cmd).getBuffer(), VK14.VK_PRIMITIVE_TOPOLOGY_LINE_LIST);
        DebugContext.line_commands.forEach(c -> c.draw(vc));
        vc.draw();

        cmd.endRendering();
    }

}

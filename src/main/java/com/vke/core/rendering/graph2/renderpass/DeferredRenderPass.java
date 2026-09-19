package com.vke.core.rendering.graph2.renderpass;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.core.color.RgbColor;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.rendering.graph2.GraphTexture;
import com.vke.core.rendering.graph2.RenderGraph;
import com.vke.core.rendering.pipeline.RenderPipelines;
import com.vke.core.rendering.vulkan.command.VulkanCmdBuffers;
import com.vke.demo.DemoScene;
import com.vke.utils.DrawUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.NVDeviceDiagnosticCheckpoints;

import java.util.List;

public class DeferredRenderPass extends RenderPass {
    private GraphTexture colorOut;

    private GraphTexture normal;
    private GraphTexture matIdx;
    private GraphTexture meshUvs;

    private GraphTexture depthOut;

    public DeferredRenderPass(RenderSystem renderSystem, RenderGraph graph, Def def) {
        super(renderSystem, graph, def);
    }

    @Override
    public void onLoad() {
        colorOut = searchOutputTexture("colorOut");

        normal = searchOutputTexture("gbuf_normal");
        matIdx = searchOutputTexture("gbuf_material_idx");
        meshUvs = searchOutputTexture("gbuf_mesh_uvs");

        depthOut = searchOutputTexture("depthOut");
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        Texture gbuf_normal = normal.extract();
        Texture gbuf_material_idx = matIdx.extract();
        Texture gbuf_mesh_uvs = meshUvs.extract();

        Texture depthOut = this.depthOut.extract();

        this.beginRendering(cmd, List.of(normal, matIdx, meshUvs), this.depthOut,
                List.of(RgbColor.BLACK, RgbColor.INVALID, RgbColor.BLACK), RgbColor.WHITE);

        int inst = context.get("inst");
        RenderPipelines.DEFERRED.setLocal(context.get("mats"));
        RenderPipelines.DEFERRED.use();
        DemoScene.MESH.drawInstanced(inst);

        cmd.endRendering();

        gbuf_normal.useInShader();
        gbuf_material_idx.useInShader();
        gbuf_mesh_uvs.useInShader();
        depthOut.useInShader();

        this.beginRendering(cmd, List.of(colorOut), RgbColor.BLACK);

        RenderPipelines.DEFERRED_LIGHT_PASS.set(gbuf_normal, gbuf_material_idx, gbuf_mesh_uvs, depthOut);
        RenderPipelines.DEFERRED_LIGHT_PASS.use();

        DrawUtils.fullscreenTri(cmd);

        cmd.endRendering();
    }

}

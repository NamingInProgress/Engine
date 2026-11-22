package com.vke.impl.rendering.pass;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.other.CISResource;
import com.vke.core.color.RgbColor;
import com.vke.core.rendering.Samplers;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.rendering.graph2.GraphTexture;
import com.vke.core.rendering.graph2.RenderGraph;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.scene.env.EnvironmentMap;

import java.io.IOException;
import java.util.List;

public class SkyboxRenderPass extends RenderPass {

    private GraphTexture color, depth;

    private final AssetHandle<RenderPipeline> pipeline = R.pipelines.get("skybox.pipeline.json");
    private CISResource map;

    public SkyboxRenderPass(RenderSystem sys, RenderGraph graph, Def def) {
        super(sys, graph, def);

    }

    @Override
    public void onLoad() {
        this.color = searchOutputTexture("colorOut");
        this.depth = searchOutputTexture("depthOut");

        try {
            RenderPipeline p = pipeline.acquire(sys);
            this.map = p.resource("u_Skybox");
        } catch (IOException e) {
            sys.throwException(e, "SkyboxRenderPass");
        }
    }

    @Override
    public void execute(CommandBuffer cmd, GraphContext context) {
        EnvironmentMap em = getCurrenScene().getEnvironment().getEnvironmentMap();

        this.beginRendering(cmd, List.of(color), depth, RgbColor.WHITE, RgbColor.WHITE);

        if (em != null) {
            cmd.bindPipeline(pipeline);
            map.set(em.getSkyboxView(), Samplers.NEAREST);
            cmd.bindDescriptorSets(pipeline);

            cmd.draw(36, 1, 0, 0);
        }

        cmd.endRendering();
    }
}

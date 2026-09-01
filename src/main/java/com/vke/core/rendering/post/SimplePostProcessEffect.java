package com.vke.core.rendering.post;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.renderer.pipeline.resource.other.CISResource;
import com.vke.core.Identifier;
import com.vke.core.rendering.Samplers;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.utils.DrawUtils;

import java.io.IOException;

public class SimplePostProcessEffect extends PostProcessEffect {
    protected final AssetHandle<? extends RenderPipeline> pipelineHandle;
    protected RenderPipeline pipeline;
    protected CISResource u_ColorTex;

    public SimplePostProcessEffect(Identifier identifier, RenderSystem renderSystem, RenderPassInstance instance, AssetHandle<? extends RenderPipeline> pipelineHandle) {
        super(identifier, renderSystem, instance);
        this.pipelineHandle = pipelineHandle;
    }

    @Override
    public void onInitialize() {
        try {
            this.pipeline = pipelineHandle.acquire(renderSystem);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.u_ColorTex = pipeline.resource("u_ColorTex");
        onInitEffect();
    }

    protected void onInitEffect() {}

    @Override
    public void draw(CommandBuffer cmd, GraphContext ctx, Texture colorInput) {
        setupUniforms(colorInput);
        cmd.bindPipeline(pipelineHandle);
        cmd.bindDescriptorSets(pipelineHandle);

        DrawUtils.fullscreenTri(cmd);
    }

    protected void setupUniforms(Texture colorInput) {
        u_ColorTex.nextWrite();
        u_ColorTex.set(colorInput, Samplers.LINEAR);
    }
}

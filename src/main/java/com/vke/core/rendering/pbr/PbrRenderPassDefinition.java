package com.vke.core.rendering.pbr;

import com.vke.api.assets.AssetHandle;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.pipeline.RenderPipeline;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.core.rendering.graph.def.RenderPassDefinition;
import com.vke.core.rendering.graph.def.SpecializedRenderPassDefinition;

import java.util.List;

public class PbrRenderPassDefinition extends SpecializedRenderPassDefinition {

    private final AssetHandle<RenderPipeline> pipeline;

    public PbrRenderPassDefinition(String name,
                                   Class<?> clazz,
                                   List<RenderPassDefinition.InputTextureDefinition> inputs,
                                   List<OutputTextureDefinition> outputs,
                                   AssetHandle<RenderPipeline> pipeline) {
        super(name, clazz, inputs, outputs);
        this.pipeline = pipeline;
    }

    @Override
    public RenderPass create(RenderSystem sys, RenderPassInstance instance) {
        return new PbrRenderPass(sys, instance, pipeline);
    }
}

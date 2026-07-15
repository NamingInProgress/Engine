package com.vke.core.rendering.graph.def;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.rendering.graph.RenderPassInstance;

import java.util.List;

public abstract class SpecializedRenderPassDefinition extends RenderPassDefinition {

    public SpecializedRenderPassDefinition(String name,
                                           Class<?> clazz,
                                           List<InputTextureDefinition> inputs,
                                           List<OutputTextureDefinition> outputs) {
        super(name, clazz, inputs, outputs);
    }

    public abstract RenderPass create(RenderSystem sys, RenderPassInstance instance);

}

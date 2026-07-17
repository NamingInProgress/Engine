package com.vke.core.rendering.graph.def;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.core.rendering.post.PostProcessingRenderPass;
import com.vke.utils.io.Identifier;

import java.util.ArrayList;
import java.util.List;

public class PostRenderPassDefinition extends SpecializedRenderPassDefinition {

    private final ArrayList<Identifier> stages;

    public PostRenderPassDefinition(String name,
                                    List<InputTextureDefinition> inputs,
                                    List<OutputTextureDefinition> outputs,
                                    ArrayList<Identifier> stages) {
        super(name, PostProcessingRenderPass.class, inputs, outputs);
        this.stages = stages;
    }

    @Override
    public RenderPass create(RenderSystem sys, RenderPassInstance instance) {
        return new PostProcessingRenderPass(sys, instance, stages);
    }

}

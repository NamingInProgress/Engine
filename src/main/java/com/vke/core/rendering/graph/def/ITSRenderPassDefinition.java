package com.vke.core.rendering.graph.def;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.api.rendering.abstraction.rendergraph.RenderPass;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.impl.pass.ITSRenderPass;

import java.util.List;

public class ITSRenderPassDefinition extends SpecializedRenderPassDefinition {

    public ITSRenderPassDefinition(String name, String input) {
        super(name, ITSRenderPass.class,
                List.of(new InputTextureDefinition("colorIn", input, null)),
                List.of(new OutputTextureDefinition("colorOut", null, TextureType.RENDER_TARGET, Format.BGRA8_SRGB, 0, 0, 1)));
    }

    @Override
    public RenderPass create(RenderSystem sys, RenderPassInstance instance) {
        return new ITSRenderPass(sys, instance);
    }

}

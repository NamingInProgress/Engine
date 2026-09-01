package com.vke.impl.rendering.post;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.core.Identifier;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.core.rendering.post.PostProcessEffect;

public class BloomPostEffect extends PostProcessEffect {

    public BloomPostEffect(Identifier identifier, RenderSystem renderSystem, RenderPassInstance instance) {
        super(identifier, renderSystem, instance);
    }

    @Override
    public void draw(CommandBuffer cmd, GraphContext ctx, Texture colorInput) {

    }

}

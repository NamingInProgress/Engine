package com.vke.core.rendering.post;

import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;
import com.vke.utils.io.Identifier;

import java.io.IOException;

public abstract class PostProcessEffect {
    protected final Identifier identifier;
    protected final RenderSystem renderSystem;
    protected final RenderPassInstance instance;

    public PostProcessEffect(Identifier identifier, RenderSystem renderSystem, RenderPassInstance instance) {
        this.identifier = identifier;
        this.renderSystem = renderSystem;
        this.instance = instance;
    }

    public void onInitialize() {}

    public abstract void draw(CommandBuffer cmd, GraphContext ctx, VertexConsumer<FullscreenQuadVertex> vc, Texture colorInput);
}

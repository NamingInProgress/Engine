package com.vke.core.rendering.post;

import com.vke.api.rendering.abstraction.draw.VertexConsumer;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;

import java.io.IOException;

public abstract class PostProcessEffect {
    protected final RenderSystem renderSystem;
    protected final RenderPassInstance instance;

    public PostProcessEffect(RenderSystem renderSystem, RenderPassInstance instance) {
        this.renderSystem = renderSystem;
        this.instance = instance;
    }

    public void onInitialize() {}

    public abstract void draw(CommandBuffer cmd, GraphContext ctx, VertexConsumer<FullscreenQuadVertex> vc, Texture colorInput);
}

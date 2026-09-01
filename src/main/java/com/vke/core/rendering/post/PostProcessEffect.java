package com.vke.core.rendering.post;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.core.Identifier;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderPassInstance;

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

    public abstract void draw(CommandBuffer cmd, GraphContext ctx, Texture colorInput);
}

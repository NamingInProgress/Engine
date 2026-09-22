package com.vke.core.rendering.post;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.core.Identifier;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.rendering.graph2.renderpass.RenderPass;

public abstract class PostProcessEffect {
    protected final Identifier identifier;
    protected final RenderSystem renderSystem;
    protected final RenderPass renderPass;

    public PostProcessEffect(Identifier identifier, RenderSystem renderSystem, RenderPass renderPass) {
        this.identifier = identifier;
        this.renderSystem = renderSystem;
        this.renderPass = renderPass;
    }

    public void onInitialize() {}

    public boolean autoStartRendering() { return true; }

    public abstract void draw(CommandBuffer cmd, GraphContext ctx, Texture colorInput, Texture colorOutput);
}

package com.vke.core.rendering.post;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.core.rendering.graph.RenderPassInstance;

public interface PostEffectProvider {
    PostProcessEffect buildEffect(RenderSystem sys, RenderPassInstance renderPass);
}

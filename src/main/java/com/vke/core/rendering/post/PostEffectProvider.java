package com.vke.core.rendering.post;

import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.core.rendering.graph2.renderpass.RenderPass;

public interface PostEffectProvider {
    PostProcessEffect buildEffect(RenderSystem sys, RenderPass renderPass);
}

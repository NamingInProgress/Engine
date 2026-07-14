package com.vke.api.rendering.abstraction.renderer.pipeline;

import com.vke.api.rendering.abstraction.renderer.data.Texture;

public interface RenderPipeline extends Pipeline {
    Texture getDepthTarget();
    Texture getColorTarget(int index);
}

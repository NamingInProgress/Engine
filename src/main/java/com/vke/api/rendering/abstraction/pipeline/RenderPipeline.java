package com.vke.api.rendering.abstraction.pipeline;

import com.vke.api.rendering.abstraction.data.Texture;

public interface RenderPipeline extends Pipeline {
    Texture getDepthTarget();
    Texture getColorTarget(int index);
}

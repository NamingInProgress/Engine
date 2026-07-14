package com.vke.api.rendering.abstraction.renderer.pipeline;

import com.vke.utils.io.Disposable;

public interface PipelineLayout extends Disposable {

    int pushConstantSize();
    int descriptorCount();

}

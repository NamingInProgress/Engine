package com.vke.api.rendering.abstraction.pipeline;

import com.vke.utils.io.Disposable;

public interface PipelineLayout extends Disposable {

    int pushConstantSize();
    int descriptorCount();

}

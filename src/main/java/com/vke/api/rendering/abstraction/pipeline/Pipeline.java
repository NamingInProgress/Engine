package com.vke.api.rendering.abstraction.pipeline;

import com.vke.utils.Disposable;

public interface Pipeline extends Disposable {

    PipelineLayout layout();

}
